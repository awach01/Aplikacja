package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class PhotoUploadFragment : Fragment() {

    private lateinit var storageReference: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private lateinit var progressBar: ProgressBar
    private val photos = mutableListOf<Photo>()
    private val PICK_IMAGE_REQUEST = 71
    private val STORAGE_PERMISSION_CODE = 1
    private var selectedFilePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_upload, container, false)
        val uploadButton: Button = view.findViewById(R.id.uploadButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        storageReference = FirebaseStorage.getInstance().reference
        db = FirebaseFirestore.getInstance()

        adapter = PhotoAdapter(requireContext(), photos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }

        uploadButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.SelectPicture)), PICK_IMAGE_REQUEST)
        }

        loadImages()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedFilePath = data.data
            uploadImage(selectedFilePath)
        }
    }

    private fun uploadImage(filePath: Uri?) {
        if (filePath != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val ref = storageReference.child("images/${UUID.randomUUID()}")

            progressBar.visibility = View.VISIBLE

            ref.putFile(filePath)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val image = hashMapOf(
                            "userId" to userId,
                            "url" to uri.toString(),
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("images")
                            .add(image)
                            .addOnSuccessListener {
                                Toast.makeText(context,
                                    getString(R.string.PhotoUploadSuccess), Toast.LENGTH_SHORT).show()
                                loadImages()
                            }
                            .addOnFailureListener { e ->
                                Log.w("PhotoUploadFragment", "Error adding document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PhotoUploadFragment", getString(R.string.PhotoUploadError), e)
                }
                .addOnCompleteListener {
                    progressBar.visibility = View.GONE
                }
        } else {
            Toast.makeText(context, getString(R.string.PhotoUploadErrorNoFile), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImages() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("images")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                photos.clear()
                for (document in documents) {
                    val url = document.getString("url") ?: continue
                    val timestamp = document.getTimestamp("timestamp")
                    photos.add(Photo(userId, url, timestamp))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("PhotoUploadFragment", "Error getting documents: ", exception)
            }
    }

    private fun formatTimestampToPolishTime(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return "Brak daty"

        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pl", "PL"))
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
        return dateFormat.format(date)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(context, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

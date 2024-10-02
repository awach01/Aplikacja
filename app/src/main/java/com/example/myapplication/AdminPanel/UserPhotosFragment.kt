package com.example.myapplication.AdminPanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Photo
import com.example.myapplication.PhotoAdapter
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserPhotosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private val photos = mutableListOf<Photo>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_photos, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PhotoAdapter(requireContext(), photos)
        recyclerView.adapter = adapter

        val userId = arguments?.getString("userId") ?: FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadImages(userId)
        }

        return view
    }

    private fun loadImages(userId: String) {
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
    }

    companion object {
        fun newInstance(userId: String): UserPhotosFragment {
            val fragment = UserPhotosFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}

package com.example.myapplication.Rest

import com.example.myapplication.ui.Photos.AdminFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.Appointments.AppointmentsFragment
import com.example.myapplication.PhotoUploadFragment
import com.example.myapplication.R
import com.example.myapplication.Review.ReviewFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val viewAppointmentsButton: FrameLayout = view.findViewById(R.id.frameLayout4)
        val uploadPhotosButton: FrameLayout = view.findViewById(R.id.frameLayout2)
        val contactInfoButton: FrameLayout = view.findViewById(R.id.frameLayout3)
        val reviewButton: FrameLayout = view.findViewById(R.id.frameLayout)
        val adminButton: FrameLayout = view.findViewById(R.id.frameLayoutAdmin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Ustawienie tekstu w przyciskach (TextView w FrameLayout)
        val viewAppointmentsText: TextView = viewAppointmentsButton.findViewById(R.id.buttonText)
        viewAppointmentsText.text = getString(R.string.AppointmentsView)

        val uploadPhotosText: TextView = uploadPhotosButton.findViewById(R.id.buttonText)
        uploadPhotosText.text = getString(R.string.UploadPhotos)

        val contactInfoText: TextView = contactInfoButton.findViewById(R.id.buttonText)
        contactInfoText.text = getString(R.string.ContactInfo)

        val reviewText: TextView = reviewButton.findViewById(R.id.buttonText)
        reviewText.text = getString(R.string.LeaveReview)

        // Obsługa kliknięć
        viewAppointmentsButton.setOnClickListener {
            val fragment = AppointmentsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        uploadPhotosButton.setOnClickListener {
            val fragment = PhotoUploadFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        contactInfoButton.setOnClickListener {
            val fragment = ContactFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        reviewButton.setOnClickListener {
            val fragment = ReviewFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        checkIfAdmin(adminButton)

        return view
    }

    private fun checkIfAdmin(adminButton: FrameLayout) {
        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val isAdmin = document.getBoolean("isAdmin") ?: false
                        if (isAdmin) {
                            adminButton.visibility = View.VISIBLE
                            val adminButtonText: TextView = adminButton.findViewById(R.id.buttonText)
                            adminButtonText.text = getString(R.string.AdminPanel)
                            adminButton.setOnClickListener {
                                val fragment = AdminFragment()
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        } else {
                            adminButton.visibility = View.GONE
                        }
                    } else {
                        adminButton.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    adminButton.visibility = View.GONE
                }
        }
    }
}

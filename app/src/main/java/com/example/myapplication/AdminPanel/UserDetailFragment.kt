package com.example.myapplication.AdminPanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var viewPhotosButton: Button
    private lateinit var viewMedicalHistoryButton: Button
    private val db = FirebaseFirestore.getInstance()

    private var userId: String? = null
    private var userFullName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_detail, container, false)

        userNameTextView = view.findViewById(R.id.userName)
        userEmailTextView = view.findViewById(R.id.userEmail)
        userPhoneTextView = view.findViewById(R.id.userPhone)
        viewPhotosButton = view.findViewById(R.id.viewPhotosButton)
        viewMedicalHistoryButton = view.findViewById(R.id.viewMedicalHistoryButton)

        userId = arguments?.getString("userId")
        if (userId != null) {
            loadUserDetails(userId!!)
        }

        viewPhotosButton.setOnClickListener {
            navigateToUserPhotosFragment(userId!!)
        }

        viewMedicalHistoryButton.setOnClickListener {
            navigateToUserMedicalHistoryFragment(userId!!)
        }

        return view
    }

    private fun loadUserDetails(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "User"
                    val email = document.getString("email") ?: "No email"
                    val phone = document.getString("phone") ?: "No phone"

                    userFullName = "$firstName $lastName"
                    userNameTextView.text = userFullName
                    userEmailTextView.text = email
                    userPhoneTextView.text = phone
                }
            }
    }

    private fun navigateToUserPhotosFragment(userId: String) {
        val fragment = UserPhotosFragment.newInstance(userId)
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToUserMedicalHistoryFragment(userId: String) {
        val fragment = UserMedicalHistoryFragment.newInstance(userId)
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        fun newInstance(userId: String): UserDetailFragment {
            val fragment = UserDetailFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}

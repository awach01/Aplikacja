package com.example.myapplication.Settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.Calendar.CalendarFragment
import com.example.myapplication.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePhoneFragment : Fragment() {

    private lateinit var editTextCurrentEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextNewPhone: EditText
    private lateinit var buttonConfirmChangeContainer: FrameLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_phone, container, false)

        editTextCurrentEmail = view.findViewById(R.id.editTextCurrentEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextNewPhone = view.findViewById(R.id.editTextNewPhone)
        buttonConfirmChangeContainer = view.findViewById(R.id.buttonConfirmChangeContainer)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        buttonConfirmChangeContainer.setOnClickListener {
            val currentEmail = editTextCurrentEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val newPhone = editTextNewPhone.text.toString().trim()

            if (currentEmail.isEmpty() || password.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(context, R.string.EnterAllData, Toast.LENGTH_SHORT).show()
            } else {
                reauthenticateAndChangePhone(currentEmail, password, newPhone)
            }
        }

        return view
    }

    private fun reauthenticateAndChangePhone(currentEmail: String, password: String, newPhone: String) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updatePhoneInFirestore(user.uid, newPhone)
                    } else {
                        Toast.makeText(context, R.string.AuthError, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, R.string.AuthError2, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePhoneInFirestore(userId: String, newPhone: String) {
        val userRef = db.collection("users").document(userId)
        userRef.update("phone", newPhone)
            .addOnSuccessListener {
                Toast.makeText(context,
                    getString(R.string.ChangePhoneSuccess, newPhone), Toast.LENGTH_SHORT).show()

                val intent = Intent(activity, CalendarFragment::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,
                    getString(R.string.ChangePhoneError, e.message), Toast.LENGTH_SHORT).show()
            }
    }
}

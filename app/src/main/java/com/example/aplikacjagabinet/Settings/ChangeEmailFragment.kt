package com.example.aplikacjagabinet.Settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aplikacjagabinet.LoginRegister.LoginActivity
import com.example.aplikacjagabinet.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailFragment : Fragment() {

    private lateinit var editTextCurrentEmail: EditText
    private lateinit var editTextNewEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonConfirmChangeContainer: FrameLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_email, container, false)

        editTextCurrentEmail = view.findViewById(R.id.editTextCurrentEmail)
        editTextNewEmail = view.findViewById(R.id.editTextNewEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonConfirmChangeContainer = view.findViewById(R.id.buttonConfirmChangeContainer)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        buttonConfirmChangeContainer.setOnClickListener {
            val currentEmail = editTextCurrentEmail.text.toString().trim()
            val newEmail = editTextNewEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (currentEmail.isEmpty() || newEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, getString(R.string.EnterAllData), Toast.LENGTH_SHORT).show()
            } else {
                reauthenticateAndChangeEmail(currentEmail, newEmail, password)
            }
        }

        return view
    }

    private fun reauthenticateAndChangeEmail(currentEmail: String, newEmail: String, password: String) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    updateEmailInFirestore(user.uid, newEmail)
                                } else {
                                    Toast.makeText(context,
                                        getString(
                                            R.string.ErrorChangeEmail,
                                            updateTask.exception?.message
                                        ), Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context,
                            getString(R.string.AuthError, task.exception?.message), Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, getString(R.string.AuthError2), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmailInFirestore(userId: String, newEmail: String) {
        val userRef = db.collection("users").document(userId)
        userRef.update("email", newEmail)
            .addOnSuccessListener {
                auth.signOut()
                Toast.makeText(context,
                    getString(R.string.ChangeEmailSuccess, newEmail), Toast.LENGTH_SHORT).show()


                val intent = Intent(context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                activity?.finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,
                    getString(R.string.ChangeEmailError, e.message), Toast.LENGTH_SHORT).show()
            }
    }
}

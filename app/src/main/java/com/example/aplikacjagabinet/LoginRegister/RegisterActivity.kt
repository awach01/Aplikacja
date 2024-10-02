package com.example.aplikacjagabinet.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.example.aplikacjagabinet.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonRegisterSubmit: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextPhone = findViewById(R.id.editTextPhone)
        buttonRegisterSubmit = findViewById(R.id.buttonRegisterSubmit)

        buttonRegisterSubmit.setOnClickListener {
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val phone = editTextPhone.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, R.string.AllFieldsFilled, Toast.LENGTH_SHORT).show()
            } else {
                registerUser(firstName, lastName, email, password, phone)
            }
        }

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())
    }

    private fun registerUser(firstName: String, lastName: String, email: String, password: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this, getString(R.string.RegisterSuccess), Toast.LENGTH_SHORT).show()


                    val user = auth.currentUser
                    val userId = user?.uid
                    val userData = hashMapOf(
                        "userId" to userId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "phone" to phone,
                        "isAdmin" to false
                    )

                    Log.d("RegisterActivity", "User data to save: $userData")

                    userId?.let {
                        db.collection("users").document(it).set(userData)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Log.d("RegisterActivity", "User data saved successfully")

                                    db.collection("users").document(it)
                                        .update("isAdmin", false)
                                        .addOnSuccessListener {
                                            Log.d("RegisterActivity", "isAdmin field updated successfully")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RegisterActivity", "Failed to update isAdmin field", e)
                                        }
                                } else {
                                    Log.e("RegisterActivity", "Failed to save user data", dbTask.exception)
                                }
                            }
                    }


                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    Toast.makeText(this,
                        getString(R.string.RegisterError, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    data class User(val firstName: String, val lastName: String, val email: String, val phone: String, val isAdmin: Boolean)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null && e2 != null) {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun onSwipeRight() {
        finish() //
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

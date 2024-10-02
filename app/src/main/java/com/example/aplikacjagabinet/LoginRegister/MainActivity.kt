package com.example.aplikacjagabinet.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjagabinet.Rest.MainDashboardActivity
import com.example.aplikacjagabinet.R
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Sprawdź, czy użytkownik jest zalogowany
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Użytkownik jest zalogowany, przejdź do głównego ekranu
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Użytkownik nie jest zalogowany, pokaż ekran logowania
            buttonRegister = findViewById(R.id.button_register)
            buttonLogin = findViewById(R.id.button_login)

            buttonRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            buttonLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}

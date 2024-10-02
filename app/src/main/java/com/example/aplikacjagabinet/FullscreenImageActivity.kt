package com.example.aplikacjagabinet

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullscreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_imagexxxxxxxx)

        val imageView: ImageView = findViewById(R.id.fullscreen_image_view)
        val imageUri = intent.getStringExtra("imageUri")

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(imageView)
        }

        imageView.setOnClickListener {
            finish()
        }
    }
}

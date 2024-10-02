package com.example.aplikacjagabinet.ui.Photos

data class Photo(
    val userId: String = "",
    val url: String = "",  // Upewnij się, że to pole nazywa się `url`, tak jak w Firestore
    val timestamp: com.google.firebase.Timestamp? = null
)

package com.example.aplikacjagabinet.Review

data class Review(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val positive: Boolean = false,
    val text: String = ""
)
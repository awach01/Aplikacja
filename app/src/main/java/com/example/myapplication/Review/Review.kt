package com.example.myapplication.Review

data class Review(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val positive: Boolean = false,
    val text: String = ""
)
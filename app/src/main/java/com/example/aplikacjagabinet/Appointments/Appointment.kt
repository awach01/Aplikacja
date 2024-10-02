package com.example.aplikacjagabinet.Appointments

data class Appointment(
    val userId: String = "",
    val date2: String = "",
    val timeSlot: Int = 0,
    var userName: String = "", // Dodana właściwość
    var id: String = ""
)

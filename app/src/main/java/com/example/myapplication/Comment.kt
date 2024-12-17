package com.example.myapplication

data class Comment(
    var id: String = "",  // Add this field to store the comment's unique ID
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
package com.bryan.gasaguaexpress.models

data class User(
    val id: String,
    val username: String,
    val role: String,
    val token: String
)
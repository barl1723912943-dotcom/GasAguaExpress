package com.bryan.gasaguaexpress.models

data class Order(
    val id: Int,
    val userId: String,
    val productId: Int,
    val quantity: Int,
    val total: Double,
    val status: String, // "Pendiente", "En camino", "Entregado"
    val createdAt: String
)
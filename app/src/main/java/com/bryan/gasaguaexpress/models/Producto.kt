package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("precioReferencial")
    val precio: Double,

    @SerializedName("descripcion")
    val descripcion: String? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null
)
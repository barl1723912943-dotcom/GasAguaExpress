package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class CrearPedidoRequest(
    @SerializedName("idProducto")
    val idProducto: String,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double
)
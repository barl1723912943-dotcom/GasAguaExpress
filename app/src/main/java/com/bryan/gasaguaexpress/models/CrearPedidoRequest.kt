package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class CrearPedidoRequest(
    @SerializedName("idProducto")
    val idProducto: String,

    @SerializedName("idDireccion")
    val idDireccion: String,

    @SerializedName("cantidad")
    val cantidad: Int
)
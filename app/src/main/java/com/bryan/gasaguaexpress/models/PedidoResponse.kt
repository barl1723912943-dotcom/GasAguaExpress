package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class PedidoResponse(
    @SerializedName("id") val id: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("idRepartidor") val idRepartidor: String?,
    @SerializedName("fechaEntregado") val fechaEntregado: String?,
    @SerializedName("latitud") val latitud: Double?,
    @SerializedName("longitud") val longitud: Double?
)
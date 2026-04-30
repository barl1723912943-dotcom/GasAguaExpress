package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class UbicacionResponse(
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double
)

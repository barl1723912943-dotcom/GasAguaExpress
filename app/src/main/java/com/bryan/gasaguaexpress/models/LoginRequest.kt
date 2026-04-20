package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("contraseña") // Esto vincula tu código con la Ñ del servidor
    val password: String
)
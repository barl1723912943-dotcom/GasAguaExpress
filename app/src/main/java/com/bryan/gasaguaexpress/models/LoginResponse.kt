package com.bryan.gasaguaexpress.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("user")
    val user: UserData?
)

data class UserData(
    @SerializedName("nombre")
    val nombre: String?,

    @SerializedName("rol")
    val rol: String?
)
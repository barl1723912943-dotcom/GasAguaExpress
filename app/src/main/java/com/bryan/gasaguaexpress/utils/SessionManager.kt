package com.bryan.gasaguaexpress.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("GasAguaSession", Context.MODE_PRIVATE)

    // Token JWT
    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun removeToken() {
        val editor = sharedPreferences.edit()
        editor.remove("token")
        editor.apply()
    }

    // Rol del usuario
    fun saveRole(role: String) {
        val editor = sharedPreferences.edit()
        editor.putString("role", role)
        editor.apply()
    }

    fun getRole(): String? {
        return sharedPreferences.getString("role", null)
    }

    fun removeRole() {
        val editor = sharedPreferences.edit()
        editor.remove("role")
        editor.apply()
    }

    // Método para verificar si el usuario está autenticado
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    // Limpiar toda la sesión
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
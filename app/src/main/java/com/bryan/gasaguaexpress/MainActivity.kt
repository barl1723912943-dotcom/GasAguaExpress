package com.bryan.gasaguaexpress

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bryan.gasaguaexpress.utils.SessionManager
import com.bryan.gasaguaexpress.ui.AdminActivity
import com.bryan.gasaguaexpress.ui.ClienteActivity
import com.bryan.gasaguaexpress.ui.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            val intent = when(sessionManager.getRole()) {
                "Admin" -> Intent(this, AdminActivity::class.java)
                "Cliente" -> Intent(this, ClienteActivity::class.java)
                else -> Intent(this, ClienteActivity::class.java)
            }
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
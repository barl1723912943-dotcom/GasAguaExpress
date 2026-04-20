package com.bryan.gasaguaexpress.ui
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.models.LoginRequest
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var etTelefono: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etTelefono = findViewById(R.id.etTelefono)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar ApiService
        apiService = NetworkModule.createService(ApiService::class.java)

        // Manejar clic en botón de login
        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        // Obtener credenciales
        val telefono = etTelefono.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validar campos
        if (telefono.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese teléfono y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar ProgressBar
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        // Crear objeto LoginRequest
        val loginRequest = LoginRequest(telefono, password)

        // Usar lifecycleScope para la corrutina
        lifecycleScope.launch {
            try {
                // Llamar al API
                val response = apiService.login(loginRequest)

                // Ocultar ProgressBar
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {

                        // 1. Extraemos los datos de forma segura (con el ? por si vienen nulos)
                        val token = loginResponse.token ?: ""
                        val userRole = loginResponse.user?.rol ?: "cliente"

                        // 2. Guardar sesión
                        sessionManager.saveToken(token)
                        sessionManager.saveRole(userRole)

                        // 3. Redirigir según el rol (ignorando mayúsculas/minúsculas)
                        val intent = if (userRole.equals("admin", ignoreCase = true)) {
                            Intent(this@LoginActivity, AdminActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, ClienteActivity::class.java)
                        }

                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Error: Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Ocultar ProgressBar
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
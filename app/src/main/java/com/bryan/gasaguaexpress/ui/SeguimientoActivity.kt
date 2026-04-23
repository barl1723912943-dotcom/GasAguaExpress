package com.bryan.gasaguaexpress.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SeguimientoActivity : AppCompatActivity() {
    private lateinit var tvEstado: TextView
    private lateinit var tvActualizando: TextView
    private lateinit var btnCancelar: Button
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private var seguimientoJob: Job? = null
    private var pedidoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        pedidoId = intent.getStringExtra("pedidoId") ?: ""
        Log.d("SEGUIMIENTO", "Iniciando seguimiento para pedido: $pedidoId")

        tvEstado = findViewById(R.id.tvEstado)
        tvActualizando = findViewById(R.id.tvActualizando)
        btnCancelar = findViewById(R.id.btnCancelar)

        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)

        btnCancelar.setOnClickListener { finish() }

        iniciarSeguimiento()
    }

    private fun iniciarSeguimiento() {
        seguimientoJob = lifecycleScope.launch {
            while (isActive) {
                consultarEstado()
                delay(5000)
            }
        }
    }

    private suspend fun consultarEstado() {
        try {
            val token = sessionManager.getToken() ?: ""
            val authHeader = "Bearer $token"
            val response = apiService.getPedido(authHeader, pedidoId)

            if (response.isSuccessful) {
                val pedido = response.body()
                Log.d("SEGUIMIENTO", "Estado actual: ${pedido?.estado}")

                when (pedido?.estado?.trim()?.lowercase()) {
                    "pendiente" -> {
                        tvEstado.text = "🔵 Buscando repartidor..."
                        tvActualizando.text = "Actualizando cada 5 segundos..."
                    }
                    "en_camino" -> {
                        tvEstado.text = "🟡 Tu pedido está en camino..."
                        tvActualizando.text = "El repartidor está en camino"
                    }
                    "entregado" -> {
                        tvEstado.text = "🟢 ¡Pedido entregado!"
                        tvActualizando.text = "Gracias por tu compra"
                        seguimientoJob?.cancel()
                        finish() // Nota: Quité el delay(3000) aquí porque a veces causa leaks si cierras la app antes
                    }
                    else -> {
                        tvEstado.text = "Estado: ${pedido?.estado}"
                    }
                }
            } else {
                Log.e("SEGUIMIENTO", "Error consultando estado: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SEGUIMIENTO", "Error de red: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seguimientoJob?.cancel()
        Log.d("SEGUIMIENTO", "SeguimientoActivity destruida - job cancelado")
    }
}
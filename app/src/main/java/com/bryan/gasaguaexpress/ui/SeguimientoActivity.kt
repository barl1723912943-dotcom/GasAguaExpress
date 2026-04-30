package com.bryan.gasaguaexpress.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SeguimientoActivity : AppCompatActivity() {
    private lateinit var tvEstado: TextView
    private lateinit var tvActualizando: TextView
    private lateinit var btnCancelar: Button
    private lateinit var mapView: MapView
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private var seguimientoJob: Job? = null
    private var ubicacionJob: Job? = null
    private var pedidoId: String = ""
    private var googleMap: GoogleMap? = null
    private var mostrandoMapa = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        pedidoId = intent.getStringExtra("pedidoId") ?: ""
        Log.d("SEGUIMIENTO", "Iniciando seguimiento para pedido: $pedidoId")

        tvEstado = findViewById(R.id.tvEstado)
        tvActualizando = findViewById(R.id.tvActualizando)
        btnCancelar = findViewById(R.id.btnCancelar)
        mapView = findViewById(R.id.mapView)

        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)

        btnCancelar.setOnClickListener { finish() }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            googleMap = map
        }

        iniciarSeguimientoEstado()
    }

    private fun iniciarSeguimientoEstado() {
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
                        ocultarMapa()
                    }
                    "en_camino" -> {
                        tvEstado.text = "🟡 Tu pedido está en camino..."
                        tvActualizando.text = "El repartidor está en camino"
                        mostrarMapa()
                        if (ubicacionJob == null || ubicacionJob?.isActive != true) {
                            iniciarSeguimientoUbicacion()
                        }
                    }
                    "entregado" -> {
                        tvEstado.text = "🟢 ¡Pedido entregado!"
                        tvActualizando.text = "Gracias por tu compra"
                        ocultarMapa()
                        seguimientoJob?.cancel()
                        ubicacionJob?.cancel()
                        finish()
                    }
                    else -> {
                        tvEstado.text = "Estado: ${pedido?.estado}"
                        ocultarMapa()
                    }
                }
            } else {
                Log.e("SEGUIMIENTO", "Error consultando estado: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SEGUIMIENTO", "Error de red: ${e.message}")
        }
    }

    private fun mostrarMapa() {
        if (!mostrandoMapa) {
            mapView.visibility = View.VISIBLE
            mostrandoMapa = true
        }
    }

    private fun ocultarMapa() {
        if (mostrandoMapa) {
            mapView.visibility = View.GONE
            mostrandoMapa = false
            ubicacionJob?.cancel()
        }
    }

    private fun iniciarSeguimientoUbicacion() {
        ubicacionJob = lifecycleScope.launch {
            while (isActive) {
                consultarUbicacionRepartidor()
                delay(3000)
            }
        }
    }

    private suspend fun consultarUbicacionRepartidor() {
        try {
            val token = sessionManager.getToken() ?: ""
            val authHeader = "Bearer $token"
            val response = apiService.getUbicacionRepartidor(authHeader, pedidoId)

            if (response.isSuccessful) {
                val ubicacion = response.body()
                ubicacion?.let {
                    Log.d("SEGUIMIENTO", "Ubicación repartidor: ${it.latitud}, ${it.longitud}")
                    googleMap?.let { map ->
                        val repartidorLatLng = LatLng(it.latitud, it.longitud)
                        map.clear()
                        map.addMarker(MarkerOptions().position(repartidorLatLng).title("Repartidor"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(repartidorLatLng, 15f))
                    }
                }
            } else {
                Log.e("SEGUIMIENTO", "Error consultando ubicación repartidor: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SEGUIMIENTO", "Error de red ubicación: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        seguimientoJob?.cancel()
        ubicacionJob?.cancel()
        mapView.onDestroy()
        Log.d("SEGUIMIENTO", "SeguimientoActivity destruida - jobs cancelados")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}

package com.bryan.gasaguaexpress.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.models.UbicacionRequest
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MapaRepartidorActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var btnEntregado: Button
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null
    private var pedidoId: String = ""
    private var latitudCliente: Double = 0.0
    private var longitudCliente: Double = 0.0
    private var ubicacionJob: Job? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_repartidor)

        pedidoId = intent.getStringExtra("pedidoId") ?: ""
        latitudCliente = intent.getDoubleExtra("latitudCliente", 0.0)
        longitudCliente = intent.getDoubleExtra("longitudCliente", 0.0)

        mapView = findViewById(R.id.mapView)
        btnEntregado = findViewById(R.id.btnEntregado)
        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            googleMap = map
            val clienteLatLng = LatLng(latitudCliente, longitudCliente)
            map.addMarker(MarkerOptions().position(clienteLatLng).title("Cliente"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(clienteLatLng, 15f))
        }

        btnEntregado.setOnClickListener {
            marcarComoEntregado()
        }

        iniciarEnvioUbicacion()
    }

    private fun iniciarEnvioUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        ubicacionJob = lifecycleScope.launch {
            while (isActive) {
                enviarUbicacionActual()
                delay(3000)
            }
        }
    }

    private suspend fun enviarUbicacionActual() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this@MapaRepartidorActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) return

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    lifecycleScope.launch {
                        try {
                            val token = sessionManager.getToken() ?: ""
                            val authHeader = "Bearer $token"
                            val request = UbicacionRequest(it.latitude, it.longitude)
                            val response = apiService.actualizarUbicacion(authHeader, request)
                            if (response.isSuccessful) {
                                Log.d("MAPA_REP", "Ubicación enviada: ${it.latitude}, ${it.longitude}")
                                googleMap?.let { map ->
                                    val repartidorLatLng = LatLng(it.latitude, it.longitude)
                                    map.clear()
                                    map.addMarker(MarkerOptions().position(repartidorLatLng).title("Tú"))
                                    map.addMarker(
                                        MarkerOptions().position(LatLng(latitudCliente, longitudCliente)).title("Cliente")
                                    )
                                }
                            } else {
                                Log.e("MAPA_REP", "Error enviando ubicación: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("MAPA_REP", "Excepción enviando ubicación: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MAPA_REP", "Error obteniendo ubicación: ${e.message}")
        }
    }

    private fun marcarComoEntregado() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val authHeader = "Bearer $token"
                val response = apiService.entregarPedido(authHeader, pedidoId)
                if (response.isSuccessful) {
                    Toast.makeText(this@MapaRepartidorActivity, "Pedido entregado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@MapaRepartidorActivity, "Error al marcar entregado", Toast.LENGTH_SHORT).show()
                    Log.e("MAPA_REP", "Error entregando: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MAPA_REP", "Error de red: ${e.message}")
                Toast.makeText(this@MapaRepartidorActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarEnvioUbicacion()
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
        ubicacionJob?.cancel()
        mapView.onDestroy()
        Log.d("MAPA_REP", "MapaRepartidorActivity destruida - job cancelado")
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

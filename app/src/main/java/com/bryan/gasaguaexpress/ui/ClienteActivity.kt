package com.bryan.gasaguaexpress.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.adapters.ProductoAdapter
import com.bryan.gasaguaexpress.models.CrearPedidoRequest
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class ClienteActivity : AppCompatActivity() {
    private lateinit var rvProductos: RecyclerView
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)

        rvProductos = findViewById(R.id.rvProductos)
        rvProductos.layoutManager = LinearLayoutManager(this)
        productoAdapter = ProductoAdapter(emptyList())
        rvProductos.adapter = productoAdapter

        productoAdapter.onItemClickListener = { producto ->
            obtenerUbicacionActual { location ->
                location?.let { safeLocation ->
                    val pedidoRequest = CrearPedidoRequest(
                        idProducto = producto.id,
                        cantidad = 1,
                        latitud = safeLocation.latitude,
                        longitud = safeLocation.longitude
                    )
                    enviarPedidoAlServidor(pedidoRequest)
                } ?: run {
                    Toast.makeText(this, "No podemos obtener tu ubicación, activa el GPS", Toast.LENGTH_LONG).show()
                }
            }
        }

        fetchProductos()
    }

    private fun obtenerUbicacionActual(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location)
                } else {
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setWaitForAccurateLocation(true)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdates(1)
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedLocationClient.removeLocationUpdates(this)
                            callback(result.lastLocation)
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No podemos obtener tu ubicación, activa el GPS", Toast.LENGTH_LONG).show()
                callback(null)
            }
    }

    private fun enviarPedidoAlServidor(pedidoRequest: CrearPedidoRequest) {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val authHeader = "Bearer $token"
                val response = apiService.crearPedido(authHeader, pedidoRequest)
                if (response.isSuccessful) {
                    Toast.makeText(this@ClienteActivity, "¡Pedido enviado con éxito!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ClienteActivity, "Error en el servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ClienteActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchProductos() {
        val token = sessionManager.getToken() ?: ""
        val authHeader = "Bearer $token"
        lifecycleScope.launch {
            try {
                val response = apiService.getProductos(authHeader)
                if (response.isSuccessful) {
                    val productos = response.body() ?: emptyList()
                    productoAdapter.updateData(productos)
                }
            } catch (e: Exception) {
                android.util.Log.e("GasAguaDebug", "Error al cargar productos: ${e.message}")
            }
        }
    }
}
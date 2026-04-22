package com.bryan.gasaguaexpress.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
        Log.d("GPS_DEBUG", "=== ClienteActivity NUEVA VERSION iniciada ===")
        setContentView(R.layout.activity_cliente2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)

        rvProductos = findViewById(R.id.rvProductos)
        rvProductos.layoutManager = LinearLayoutManager(this)
        productoAdapter = ProductoAdapter(emptyList())
        rvProductos.adapter = productoAdapter

        productoAdapter.onItemClickListener = { producto ->
            Log.d("GPS_DEBUG", "Botón Pedir presionado - producto: ${producto.id}")
            obtenerUbicacionActual { location ->
                if (location != null) {
                    Log.d("GPS_DEBUG", "Ubicación obtenida: lat=${location.latitude}, lon=${location.longitude}")
                    val pedidoRequest = CrearPedidoRequest(
                        idProducto = producto.id,
                        cantidad = 1,
                        latitud = location.latitude,
                        longitud = location.longitude
                    )
                    enviarPedidoAlServidor(pedidoRequest)
                } else {
                    Log.e("GPS_DEBUG", "Ubicación NULL - no se pudo obtener GPS")
                    Toast.makeText(this, "No podemos obtener tu ubicación, activa el GPS", Toast.LENGTH_LONG).show()
                }
            }
        }

        fetchProductos()
    }

    private fun obtenerUbicacionActual(callback: (Location?) -> Unit) {
        Log.d("GPS_DEBUG", "Iniciando obtención de ubicación")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GPS_DEBUG", "Permiso de ubicación NO otorgado - solicitando")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        Log.d("GPS_DEBUG", "Permiso OK - obteniendo lastLocation")

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.d("GPS_DEBUG", "lastLocation resultado: $location")
                if (location != null) {
                    callback(location)
                } else {
                    Log.d("GPS_DEBUG", "lastLocation es null - activando fallback")
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 5000
                    )
                        .setWaitForAccurateLocation(true)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdates(1)
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            Log.d("GPS_DEBUG", "fallback resultado: ${result.lastLocation}")
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
            .addOnFailureListener { e ->
                Log.e("GPS_DEBUG", "Error obteniendo ubicación: ${e.message}")
                Toast.makeText(
                    this,
                    "No podemos obtener tu ubicación, activa el GPS",
                    Toast.LENGTH_LONG
                ).show()
                callback(null)
            }
    }

    private fun enviarPedidoAlServidor(pedidoRequest: CrearPedidoRequest) {
        Log.d("GPS_DEBUG", "Enviando pedido - lat: ${pedidoRequest.latitud}, lon: ${pedidoRequest.longitud}")
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val authHeader = "Bearer $token"
                val response = apiService.crearPedido(authHeader, pedidoRequest)
                if (response.isSuccessful) {
                    Log.d("GPS_DEBUG", "Pedido creado exitosamente")
                    Toast.makeText(
                        this@ClienteActivity,
                        "¡Pedido enviado con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("GPS_DEBUG", "Error servidor: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(
                        this@ClienteActivity,
                        "Error en el servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("GPS_DEBUG", "Excepción de red: ${e.message}")
                Toast.makeText(
                    this@ClienteActivity,
                    "Error de red: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
                Log.e("GasAguaDebug", "Error al cargar productos: ${e.message}")
            }
        }
    }
}
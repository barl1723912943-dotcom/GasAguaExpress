package com.bryan.gasaguaexpress.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.adapters.ProductoAdapter
import com.bryan.gasaguaexpress.models.CrearPedidoRequest
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import kotlinx.coroutines.launch

class ClienteActivity : AppCompatActivity() {
    private lateinit var rvProductos: RecyclerView
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    // Dirección temporal para pruebas
    private val DIRECCION_TEMPORAL = "550e8400-e29b-41d4-a716-446655440000"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.e("PRUEBA_GAS", "LA ACTIVIDAD HA INICIADO")
        setContentView(R.layout.activity_cliente2)
        
        // Log de inicio de onCreate
        android.util.Log.d("GasAguaDebug", "onCreate iniciado")

        sessionManager = SessionManager(this)

        rvProductos = findViewById(R.id.rvProductos)
        rvProductos.layoutManager = LinearLayoutManager(this)

        productoAdapter = ProductoAdapter(emptyList())
        rvProductos.adapter = productoAdapter

        // Configurar el listener para los clics en los productos
        productoAdapter.onItemClickListener = { producto ->
            // Crear un pedido con cantidad fija de 1 para pruebas
            val pedidoRequest = CrearPedidoRequest(
                idProducto = producto.id,
                idDireccion = DIRECCION_TEMPORAL,
                cantidad = 1
            )

            // Iniciar la corrutina para crear el pedido
            lifecycleScope.launch {
                try {
                    val token = sessionManager.getToken() ?: ""
                    val authHeader = "Bearer $token"
                    val response = apiService.crearPedido(authHeader, pedidoRequest)
                    
                    if (response.isSuccessful) {
                        // Pedido creado exitosamente
                        Toast.makeText(this@ClienteActivity, "Pedido creado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Error al crear el pedido
                        Toast.makeText(this@ClienteActivity, "Error al crear el pedido", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Log de error de red
                    android.util.Log.e("GasAguaDebug", "Error de red: ${e.message}")
                    // Mostrar mensaje de error
                    Toast.makeText(this@ClienteActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        apiService = NetworkModule.createService(ApiService::class.java)

        fetchProductos()
    }

    private fun fetchProductos() {
        // Log de inicio de fetchProductos
        android.util.Log.d("GasAguaDebug", "fetchProductos iniciado")
        
        val token = sessionManager.getToken() ?: ""
        // Log del token
        android.util.Log.d("GasAguaDebug", "Token: $token")
        
        val authHeader = "Bearer $token"
        Toast.makeText(this, "Mi token es: $token", Toast.LENGTH_LONG).show()
        
        // Log de inicio de fetchProductos
        android.util.Log.d("GasAguaDebug", "fetchProductos iniciado")

        lifecycleScope.launch {
            try {
                val response = apiService.getProductos(authHeader)
                android.util.Log.e("PRUEBA_GAS", "RESULTADO API: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    // Log de respuesta exitosa
                    android.util.Log.d("GasAguaDebug", "Respuesta: ${response.code()}")
                    
                    val productos = response.body() ?: emptyList()
                    productoAdapter.updateData(productos)
                    
                    // Log de cantidad de productos recibidos
                    android.util.Log.d("GasAguaDebug", "Número de productos recibidos: ${productos.size}")
                } else {
                    Toast.makeText(this@ClienteActivity, "Error ${response.code()}: No autorizado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ClienteActivity, "Sin conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
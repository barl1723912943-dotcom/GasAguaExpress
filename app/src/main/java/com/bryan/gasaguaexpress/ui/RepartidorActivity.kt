package com.bryan.gasaguaexpress.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.network.ApiService
import com.bryan.gasaguaexpress.network.NetworkModule
import com.bryan.gasaguaexpress.utils.SessionManager
import com.bryan.gasaguaexpress.adapters.PedidoRepartidorAdapter
import com.bryan.gasaguaexpress.models.PedidoResponse
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.bryan.gasaguaexpress.databinding.ItemPedidoRepartidorBinding


class RepartidorActivity : AppCompatActivity() {
    private lateinit var rvPedidosPendientes: RecyclerView
    private lateinit var adapter: PedidoRepartidorAdapter
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repartidor)

        // Inicializar RecyclerView
        rvPedidosPendientes = findViewById(R.id.rvPedidosPendientes)
        rvPedidosPendientes.layoutManager = LinearLayoutManager(this)
        adapter = PedidoRepartidorAdapter(emptyList()) { pedido ->
            aceptarPedido(pedido)
        }
        rvPedidosPendientes.adapter = adapter

        // Inicializar SessionManager y ApiService
        sessionManager = SessionManager(this)
        apiService = NetworkModule.createService(ApiService::class.java)

        // Cargar pedidos pendientes
        cargarPedidos()
    }

    private fun cargarPedidos() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val authHeader = "Bearer $token"
                val response = apiService.getPedidosPendientes(authHeader)

                if (response.isSuccessful) {
                    val pedidos = response.body() ?: emptyList()
                    Log.d("REPARTIDOR", "Pedidos cargados: ${pedidos.size}")
                    adapter.updateData(pedidos)
                } else {
                    Log.e("REPARTIDOR", "Error al cargar pedidos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("REPARTIDOR", "Error de red: ${e.message}")
            }
        }
    }

    private fun aceptarPedido(pedido: PedidoResponse) {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val authHeader = "Bearer $token"
                val response = apiService.aceptarPedido(authHeader, pedido.id)

                if (response.isSuccessful) {
                    Toast.makeText(this@RepartidorActivity, "Pedido Aceptado", Toast.LENGTH_SHORT).show()
                    Log.d("REPARTIDOR", "Pedido aceptado: ${pedido.id}")

                    val intent = android.content.Intent(this@RepartidorActivity, MapaRepartidorActivity::class.java).apply {
                        putExtra("pedidoId", pedido.id)
                        putExtra("latitudCliente", pedido.latitud ?: 0.0)
                        putExtra("longitudCliente", pedido.longitud ?: 0.0)
                    }
                    startActivity(intent)
                } else {
                    Log.e("REPARTIDOR", "Error al aceptar pedido: ${response.code()}")
                    Toast.makeText(this@RepartidorActivity, "Error al aceptar el pedido", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("REPARTIDOR", "Error al aceptar pedido: ${e.message}")
                Toast.makeText(this@RepartidorActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
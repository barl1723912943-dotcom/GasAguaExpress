package com.bryan.gasaguaexpress.network
import com.bryan.gasaguaexpress.models.LoginRequest
import com.bryan.gasaguaexpress.models.LoginResponse
import com.bryan.gasaguaexpress.models.Producto
import com.bryan.gasaguaexpress.models.CrearPedidoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/productos")
    suspend fun getProductos(@Header("Authorization") token: String): Response<List<Producto>>

    @POST("api/Pedidos")
    suspend fun crearPedido(
        @Header("Authorization") token: String,
        @Body pedidoRequest: CrearPedidoRequest
    ): Response<Unit>
}
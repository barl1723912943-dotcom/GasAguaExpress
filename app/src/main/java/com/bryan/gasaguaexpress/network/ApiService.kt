package com.bryan.gasaguaexpress.network
import com.bryan.gasaguaexpress.models.LoginRequest
import com.bryan.gasaguaexpress.models.LoginResponse
import com.bryan.gasaguaexpress.models.Producto
import com.bryan.gasaguaexpress.models.CrearPedidoRequest
import com.bryan.gasaguaexpress.models.PedidoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/productos")
    suspend fun getProductos(@Header("Authorization") token: String): Response<List<Producto>>

    @GET("api/Pedidos/pendientes")
    suspend fun getPedidosPendientes(@Header("Authorization") token: String): Response<List<PedidoResponse>>

    @PUT("api/Pedidos/{id}/aceptar")
    suspend fun aceptarPedido(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/Pedidos")
    suspend fun crearPedido(
        @Header("Authorization") token: String,
        @Body pedidoRequest: CrearPedidoRequest
    ): Response<PedidoResponse>

    @GET("api/Pedidos/{id}")
    suspend fun getPedido(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<PedidoResponse>
}
# GasAguaAPI — Guía para Agentes de Código

> Este archivo está escrito en español porque todo el código, comentarios, mensajes de API y documentación del proyecto usan español como idioma principal.

## Descripción General

**GasAguaAPI** es la API backend de *GasAguaExpress*, una aplicación de entrega a domicilio de gas doméstico y agua embotellada. Está desarrollada con **ASP.NET Core (.NET 10)** y expone endpoints REST para la gestión de usuarios, pedidos, direcciones, repartidores y catálogo de productos.

La API utiliza autenticación basada en **JWT Bearer**, roles de usuario (`cliente`, `repartidor`, `admin`) y **Entity Framework Core** con SQL Server como base de datos.

---

## Tecnología y Arquitectura

| Capa | Tecnología / Paquete | Versión |
|------|----------------------|---------|
| Framework | .NET SDK (Web) | `net10.0` |
| ORM | Entity Framework Core SQL Server | `10.0.5` |
| Auth | ASP.NET Core JWT Bearer | `10.0.5` |
| Password Hashing | BCrypt.Net-Next | `4.1.0` |
| OpenAPI | Swashbuckle.AspNetCore | `7.0.0` |

Características del proyecto (`GasAguaAPI.csproj`):
- `Nullable` habilitado.
- `ImplicitUsings` habilitado.

### Estructura de Carpetas

```
Controllers/          → Endpoints de la API (Auth, Direccion, Pedidos, Productos, Repartidor)
Data/                 → DbContext (AppDbContext.cs)
Extensions/           → Métodos de extensión sobre WebApplication (CatalogoExtension.cs)
Models/               → Entidades de EF Core con anotaciones de datos
Services/             → Lógica de negocio desacoplada (ICatalogoService, CatalogoService)
Scripts/              → Scripts auxiliares (InicializarProductos.cs)
Properties/           → launchSettings.json
```

---

## Configuración y Ejecución

### Requisitos
- SDK de .NET 10 instalado.
- SQL Server local (o accesible) con la base de datos `GasAguaExpress`.

### Comandos Principales

```bash
# Restaurar paquetes
 dotnet restore

# Compilar
 dotnet build

# Ejecutar en desarrollo (HTTP en 0.0.0.0:5017)
 dotnet run --launch-profile http

# Ejecutar con HTTPS
 dotnet run --launch-profile https
```

### Configuración de Entornos

- **`appsettings.json`**: contiene la cadena de conexión a SQL Server (`DefaultConnection`) y la clave JWT (`Jwt:Key`).
- **`appsettings.Development.json`**: solo sobrescribe niveles de logging.
- **`launchSettings.json`**: define los perfiles `http` (puerto 5017) e `https` (puertos 7035/5017).

> **Importante**: el JWT key actual está hardcodeado para desarrollo local. En producción debe inyectarse mediante variables de entorno o un secret manager. La validación de `Issuer` y `Audience` está desactivada en desarrollo.

---

## Convenciones de Código

### Idioma
- **Todo el dominio, comentarios y mensajes de respuesta de la API están en español.**
- Nombres de clases, propiedades y métodos usan español (p. ej., `ContraseñaHash`, `FechaRegistro`, `RepartidorInfo`).

### Estilo
- **PascalCase** para clases, métodos y propiedades públicas.
- **camelCase** para parámetros y variables locales.
- **snake_case** en nombres de columnas de base de datos; se mapean con `HasColumnName` en `OnModelCreating` o con el atributo `[Column("nombre_columna")]`.
- Los **DTOs** se definen dentro del mismo archivo del controlador que los usa (no hay carpeta dedicada de DTOs).
- Las claves primarias son siempre `Guid`.

### Ejemplo de Entidad

```csharp
[Table("Productos")]
public class Producto
{
    [Key]
    public Guid Id { get; set; }

    [Required]
    [StringLength(100)]
    public string Nombre { get; set; } = null!;

    [Column("precio_referencial")]
    public decimal PrecioReferencial { get; set; }
}
```

---

## Autenticación y Autorización

La API usa **JWT Bearer** con las siguientes reglas:

- El token se genera en `AuthController` al hacer login.
- Tiempo de expiración: **7 días**.
- Claims incluidos: `NameIdentifier` (Guid del usuario), `Name`, `Role`, `telefono`.
- Los controladores leen el rol y el identificador desde `User.FindFirst(ClaimTypes.Role)` y `User.FindFirst(ClaimTypes.NameIdentifier)`.

### Roles y Permisos

| Rol | Permisos Típicos |
|-----|------------------|
| `cliente` | Crear pedidos, ver sus propios pedidos, gestionar sus direcciones. |
| `repartidor` | Ver pedidos pendientes, aceptar/asignar pedidos, actualizar su ubicación. |
| `admin` | Crear productos en el catálogo (actualmente el único endpoint protegido para admin). |

---

## Modelo de Datos

Las siguientes tablas están mapeadas en `AppDbContext`:

- **Usuarios** — clientes, repartidores y administradores.
- **Direcciones** — direcciones de entrega asociadas a un usuario.
- **Productos** — catálogo de gas y agua.
- **Pedidos** — órdenes de compra con estado (`pendiente`, `en_camino`, `entregado`, `cancelado`). Los estados se guardan siempre en minúsculas con guión bajo.
- **Repartidores_Info** — datos operativos del repartidor (ubicación, deuda total, entregas). Incluye `UltimaLatitud`, `UltimaLongitud` y `UltimaActualizacion` para tracking GPS en tiempo real.
- **Deudas** — deudas generadas por pedido (monto fijo de `$0.20`).
- **Pagos** — pagos realizados por repartidores, con confirmación de admin.

> El DbContext usa `OnModelCreating` para mapear explícitamente nombres de columnas en snake_case y tipos decimales (`decimal(9,6)` para latitud/longitud).

---

## Endpoints Principales

| Controlador | Ruta | Método | Descripción | Seguridad |
|-------------|------|--------|-------------|-----------|
| Auth | `api/auth/register` | POST | Registro de usuario | Público |
| Auth | `api/auth/login` | POST | Login y obtención de JWT | Público |
| Direccion | `api/direcciones` | GET | Listar direcciones del usuario autenticado | JWT |
| Direccion | `api/direcciones` | POST | Crear dirección para el usuario autenticado | JWT |
| Pedidos | `api/pedidos` | GET | Listar pedidos del cliente autenticado | JWT + rol `cliente` |
| Pedidos | `api/pedidos` | POST | Crear un nuevo pedido | JWT + rol `cliente` |
| Pedidos | `api/pedidos/pendientes` | GET | Listar pedidos sin repartidor | JWT + rol `repartidor` |
| Pedidos | `api/pedidos/{id}/aceptar` | PUT | Aceptar un pedido (asigna repartidor) | JWT + rol `repartidor` |
| Pedidos | `api/pedidos/{id}/asignar-repartidor` | PUT | Asignar repartidor y cambiar estado | JWT + rol `repartidor` |
| Productos | `api/productos` | GET | Listar todos los productos | Público |
| Productos | `api/productos` | POST | Agregar un producto | JWT + rol `admin` |
| Pedidos | `api/pedidos/{id}/entregar` | PUT | Marcar pedido como entregado (rol repartidor) | JWT + rol `repartidor` |
| Repartidor | `api/repartidor/ubicacion` | PUT | Actualizar ubicación del repartidor | JWT |
| Repartidor | `api/repartidor/{pedidoId}/ubicacion-repartidor` | GET | Consultar ubicación del repartidor asignado a un pedido | JWT |

Swagger está disponible en desarrollo en `/swagger`.

---

## Catálogo de Productos

El proyecto incluye un servicio (`CatalogoService`) para resetear e inicializar el catálogo con los productos reales de la zona de El Empalme:

1. **Gas Doméstico** — $2.75 (cilindro retornable de 15 kg)
2. **Agua Pure Water** — $2.50 (botellón 20 L, mejor calidad)
3. **Agua Normal** — $1.00 (botellón 20 L, estándar)

Para inicializar el catálogo al arranque se puede usar la extensión:

```csharp
await app.InicializarCatalogo();
```

También existe un script independiente en `Scripts/InicializarProductos.cs`.

---

## Pruebas

- **No hay proyectos de prueba configurados** en la solución actual.
- No se referencian xUnit, NUnit ni MSTest.
- Si se agregan tests, se recomienda crear un proyecto `GasAguaAPI.Tests` con xUnit y `Microsoft.EntityFrameworkCore.InMemory` para probar la capa de servicios sin tocar la base de datos real.

---

## Consideraciones de Seguridad

- La clave JWT está visible en `appsettings.json`. En producción debe provenir de variables de entorno o Azure Key Vault / AWS Secrets Manager.
- La cadena de conexión usa `Trusted_Connection=True` (autenticación de Windows de la máquina de desarrollo).
- `ValidateIssuer` y `ValidateAudience` están desactivados en el middleware JWT (comentado para desarrollo local).
- `UseHttpsRedirection` está comentado en `Program.cs`; la API actualmente corre solo en HTTP en el perfil por defecto.
- El hashing de contraseñas usa **BCrypt** (configuración por defecto del paquete `BCrypt.Net-Next`).

---

## Despliegue

El proyecto no cuenta con configuraciones de CI/CD, Dockerfile ni manifiestos de Kubernetes. Para desplegar:

1. Publicar con `dotnet publish -c Release -o ./publish`.
2. Configurar las variables de entorno:
   - `ConnectionStrings__DefaultConnection`
   - `Jwt__Key`
3. Ejecutar el binario publicado o hostearlo en IIS / Azure App Service / contenedor Linux.

---

## Frontend Móvil — GasAguaExpress (Android)

El proyecto cuenta con una aplicación cliente Android ubicada en `C:\Users\User\AndroidStudioProjects\GasAguaExpress`. A continuación se detalla su estructura y tecnología para que los cambios en la API se alineen con el consumo real del frontend.

### Tecnología y Dependencias

| Capa | Tecnología / Paquete | Versión |
|------|----------------------|---------|
| Lenguaje | Kotlin | 100 % |
| UI | Android Views / XML | — |
| Networking | Retrofit 2 + Gson Converter | `2.9.0` |
| HTTP Client | OkHttp + Logging Interceptor | `4.11.0` |
| Imágenes | Glide | `4.15.1` |
| Location | Play Services Location | `21.0.1` |
| Maps | Google Maps SDK | `18.2.0` |
| Lifecycle | ViewModel / LiveData KTX | `2.6.1` |
| Build features | `viewBinding`, `dataBinding`, `buildConfig` | habilitados |

### Estructura de Carpetas (app/src/main)

```
java/com/bryan/gasaguaexpress/
├── MainActivity.kt
├── adapters/
│   ├── PedidoRepartidorAdapter.kt
│   └── ProductoAdapter.kt
├── models/
│   ├── CrearPedidoRequest.kt
│   ├── LoginRequest.kt
│   ├── LoginResponse.kt
│   ├── Order.kt
│   ├── PedidoResponse.kt
│   ├── Producto.kt
│   ├── UbicacionRequest.kt
│   ├── UbicacionResponse.kt
│   └── User.kt
├── network/
│   ├── ApiService.kt
│   ├── NetworkModule.kt
│   └── RetrofitClient.kt   (legacy, no usado activamente)
├── ui/
│   ├── AdminActivity.kt
│   ├── ClienteActivity.kt
│   ├── LoginActivity.kt
│   ├── MapaRepartidorActivity.kt
│   ├── RepartidorActivity.kt
│   └── SeguimientoActivity.kt
├── utils/
│   ├── Extensions.kt
│   ├── Resource.kt         (definido pero no usado)
│   └── SessionManager.kt
└── viewmodel/
    └── MainViewModel.kt    (vacío, lógica en Activities)

res/layout/
├── activity_*.xml          (Login, Cliente, Repartidor, Seguimiento, Admin, MapaRepartidor)
├── item_producto.xml
└── item_pedido_repartidor.xml
```

### Conexión a la API

La URL base está hardcodeada en `network/NetworkModule.kt`:

```kotlin
private const val BASE_URL = "http://192.168.1.4:5017/"
```

Cada Activity crea su propia instancia del servicio mediante:

```kotlin
apiService = NetworkModule.createService(ApiService::class.java)
```

**Endpoints consumidos por el cliente** (`ApiService.kt`):

| Método | Ruta | Usado en |
|--------|------|----------|
| POST | `api/auth/login` | `LoginActivity` |
| GET | `api/productos` | `ClienteActivity` |
| GET | `api/Pedidos/pendientes` | `RepartidorActivity` |
| PUT | `api/Pedidos/{id}/aceptar` | `RepartidorActivity` |
| POST | `api/Pedidos` | `ClienteActivity` |
| GET | `api/Pedidos/{id}` | `SeguimientoActivity` (polling cada 5 s) |
| PUT | `api/repartidor/ubicacion` | `MapaRepartidorActivity` (cada 3 s) |
| GET | `api/repartidor/{pedidoId}/ubicacion-repartidor` | `SeguimientoActivity` (cada 3 s cuando está en camino) |
| PUT | `api/Pedidos/{id}/entregar` | `MapaRepartidorActivity` (botón entregado) |

> **Nota**: la aplicación no utiliza `RetrofitClient.kt` (usa callbacks `Call<T>`); el cliente activo es `NetworkModule.kt` con funciones `suspend` y corrutinas.

### Arquitectura y Patrones

- **No es MVVM estricto**: la lógica de presentación y negocio vive directamente en las `Activity` (patrón "God Activity").
- **No existe capa Repository**: las Activities llaman a `apiService` dentro de `lifecycleScope.launch`.
- **ViewModel vacío**: `MainViewModel.kt` solo extiende `ViewModel` sin lógica; un comentario indica que la lógica reside en `LoginActivity`.
- **Polling de seguimiento**: `SeguimientoActivity` consulta el estado del pedido cada 5 segundos. Cuando el estado es `en_camino`, muestra un `MapView` y consulta la ubicación del repartidor cada 3 segundos.
- **GPS en repartidor**: `MapaRepartidorActivity` envía la ubicación GPS del repartidor al backend cada 3 segundos vía `actualizarUbicacion()`, y muestra el mapa con el marcador del cliente.
- **GPS en cliente**: `ClienteActivity` usa `FusedLocationProviderClient` para obtener la ubicación del usuario al crear un pedido.

### Consideraciones para el Backend

- Mantener las rutas de la API y los nombres de propiedades en los JSON **estables**, porque el cliente Android usa modelos Kotlin manuales mapeados con Gson.
- Si se renombra un campo en una respuesta JSON, es necesario actualizar la clase correspondiente en `app/src/main/java/.../models/`.
- El cliente espera que los códigos de estado HTTP sean los estándar (`200`, `201`, `400`, `401`, `404`) para manejar errores básicos en las Activities.
- La app usa **HTTP plano** (`http://`) en desarrollo; cualquier cambio a HTTPS requiere actualizar `BASE_URL` y considerar el certificado en el emulador/dispositivo.
- **Google Maps API Key**: se lee desde `local.properties` bajo la clave `maps.api.key` y se inyecta en el `AndroidManifest.xml` vía `manifestPlaceholders` en `build.gradle.kts`. Reemplazar `MAPS_API_KEY_PLACEHOLDER` por la API key real de Google Cloud Console.

---

## Notas para el Agente

- Mantener los mensajes de respuesta de la API en **español**.
- Respetar la convención de nombres de columnas en **snake_case** al modificar entidades o el DbContext.
- Si se agrega un nuevo controlador, seguir el patrón `[ApiController]` + `[Route("api/[controller]")]`.
- Los DTOs pueden seguirse definiendo dentro del archivo del controlador, a menos que se decida crear una carpeta `Dtos/` como refactorización posterior.
- Antes de agregar migraciones de EF Core, verificar que `Microsoft.EntityFrameworkCore.Design` está presente (ya lo está).

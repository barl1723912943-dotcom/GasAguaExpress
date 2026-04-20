# Inicialización del Catálogo de Productos

Este documento describe cómo inicializar el catálogo de productos con los precios reales de El Empalme.

## Pasos para inicializar el catálogo

Para inicializar el catálogo con los productos reales de El Empalme, puedes usar el servicio [CatalogoService](file:///c:/Users/Usuario/Untitled-1/GasAguaAPI/Services/CatalogoService.cs#L5-L46) que hemos creado.

### Opción 1: Inicializar al inicio de la aplicación

Si deseas inicializar el catálogo cada vez que se inicie la aplicación, puedes agregar esta línea en tu Program.cs, después de donde se crea la aplicación:

```csharp
await app.InicializarCatalogo();
```

### Opción 2: Inicializar manualmente

También puedes crear un endpoint temporal para inicializar el catálogo:

```csharp
// Agregar este código en Program.cs
app.MapPost("/admin/inicializar-catalogo", async (CatalogoService catalogoService) =>
{
    await catalogoService.ResetearCatalogoAsync();
    return Results.Ok("Catálogo inicializado correctamente.");
}).RequireAuthorization(policy => policy.RequireClaim(ClaimTypes.Role, "admin"));
```

## Productos que se van a insertar

El proceso de inicialización:

1. Elimina todos los productos existentes en la tabla Productos
2. Inserta los siguientes 3 productos exactos:

   - Nombre: 'Gas Doméstico', Precio: 2.75, Descripción: 'Cilindro retornable de gas de 15kg'
   - Nombre: 'Agua Pure Water', Precio: 2.50, Descripción: 'Botellón de mejor calidad, 20 litros'
   - Nombre: 'Agua Normal', Precio: 1.00, Descripción: 'Botellón de agua estándar, 20 litros'

## Seguridad

- El endpoint GET para listar productos es público
- El endpoint POST para agregar productos requiere rol 'admin'
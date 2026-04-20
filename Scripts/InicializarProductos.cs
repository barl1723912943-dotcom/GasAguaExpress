using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Data;
using GasAguaAPI.Models;

namespace GasAguaAPI.Scripts
{
    public class InicializarProductos
    {
        public static async Task Ejecutar(AppDbContext context)
        {
            Console.WriteLine("Iniciando proceso de inicialización de productos...");
            
            // Eliminar todos los productos existentes
            Console.WriteLine("Eliminando productos existentes...");
            var productosExistentes = await context.Productos.ToListAsync();
            context.Productos.RemoveRange(productosExistentes);
            await context.SaveChangesAsync();
            Console.WriteLine($"Se eliminaron {productosExistentes.Count} productos existentes.");

            // Crear los nuevos productos con los precios reales de El Empalme
            var productos = new List<Producto>
            {
                new Producto
                {
                    Id = Guid.NewGuid(),
                    Nombre = "Gas Doméstico",
                    PrecioReferencial = 2.75m,
                    Descripcion = "Cilindro retornable de gas de 15kg"
                },
                new Producto
                {
                    Id = Guid.NewGuid(),
                    Nombre = "Agua Pure Water",
                    PrecioReferencial = 2.50m,
                    Descripcion = "Botellón de mejor calidad, 20 litros"
                },
                new Producto
                {
                    Id = Guid.NewGuid(),
                    Nombre = "Agua Normal",
                    PrecioReferencial = 1.00m,
                    Descripcion = "Botellón de agua estándar, 20 litros"
                }
            };

            // Agregar los nuevos productos
            Console.WriteLine("Agregando nuevos productos...");
            context.Productos.AddRange(productos);
            await context.SaveChangesAsync();
            
            Console.WriteLine("Catálogo de productos inicializado exitosamente:");
            foreach (var producto in productos)
            {
                Console.WriteLine($"- {producto.Nombre}: ${producto.PrecioReferencial} - {producto.Descripcion}");
            }
        }
    }
}
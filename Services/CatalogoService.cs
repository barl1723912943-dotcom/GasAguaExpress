using GasAguaAPI.Data;
using GasAguaAPI.Models;
using Microsoft.EntityFrameworkCore;

namespace GasAguaAPI.Services
{
    public class CatalogoService : ICatalogoService
    {
        private readonly AppDbContext _context;

        public CatalogoService(AppDbContext context)
        {
            _context = context;
        }

        public async Task ResetearCatalogoAsync()
        {
            // Eliminar todos los productos existentes
            var productosExistentes = await _context.Productos.ToListAsync();
            _context.Productos.RemoveRange(productosExistentes);
            await _context.SaveChangesAsync();

            // Crear los nuevos productos
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
            _context.Productos.AddRange(productos);
            await _context.SaveChangesAsync();
        }

        public async Task<Producto?> GetProductoByIdAsync(Guid id)
        {
            return await _context.Productos.FirstOrDefaultAsync(p => p.Id == id);
        }
    }
}
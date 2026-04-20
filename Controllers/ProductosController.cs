using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Data;
using GasAguaAPI.Models;
using System.Security.Claims;

namespace GasAguaAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ProductosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ProductosController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/productos
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Producto>>> GetProductos()
        {
            var productos = await _context.Productos.ToListAsync();
            return Ok(productos);
        }

        // POST: api/productos
        [HttpPost]
        public async Task<ActionResult<Producto>> PostProducto(Producto producto)
        {
            // Verificar si el usuario tiene rol 'admin'
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "admin")
            {
                return Forbid(); // O Unauthorized si prefieres
            }

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            producto.Id = Guid.NewGuid();
            _context.Productos.Add(producto);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetProductos), new { id = producto.Id }, producto);
        }
    }
}
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Data;
using GasAguaAPI.Models;
using GasAguaAPI.Services;
using System.Security.Claims;

namespace GasAguaAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PedidosController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly ICatalogoService _catalogoService;

        public PedidosController(AppDbContext context, ICatalogoService catalogoService)
        {
            _context = context;
            _catalogoService = catalogoService;
        }

        // POST: api/pedidos
        [HttpPost]
        public async Task<ActionResult<Pedido>> PostPedido(CrearPedidoDto pedidoDto)
        {
            // Verificar si el usuario tiene rol 'cliente'
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "cliente")
            {
                return Forbid();
            }

            // Extraer el id_cliente del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            var idCliente = Guid.Parse(userIdClaim.Value);

            // Validar que el producto exista usando el servicio de catálogo
            var producto = await _catalogoService.GetProductoByIdAsync(pedidoDto.IdProducto);
            if (producto == null)
            {
                return BadRequest("El producto especificado no existe.");
            }

            // Crear el pedido
            var pedido = new Pedido
            {
                Id = Guid.NewGuid(),
                IdCliente = idCliente,
                IdProducto = pedidoDto.IdProducto,
                IdRepartidor = null,
                Estado = "Pendiente",
                FechaCreado = DateTime.UtcNow,
                Latitud = pedidoDto.Latitud,
                Longitud = pedidoDto.Longitud
            };

            _context.Pedidos.Add(pedido);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetPedidos), new { id = pedido.Id }, pedido);
        }

        // GET: api/pedidos/{id}
        [HttpGet("{id}")]
        public async Task<ActionResult<Pedido>> GetPedidoById(Guid id)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Usuario no autenticado");

            var pedido = await _context.Pedidos.FindAsync(id);
            if (pedido == null)
                return NotFound("Pedido no encontrado");

            return Ok(pedido);
        }

        // GET: api/pedidos/pendientes
        [HttpGet("pendientes")]
        public async Task<ActionResult<IEnumerable<Pedido>>> GetPedidosPendientes()
        {
            // Verificar si el usuario tiene rol 'repartidor'
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
            {
                return Forbid();
            }

            var pedidosPendientes = await _context.Pedidos
                .Where(p => p.Estado.ToLower() == "pendiente")
                .ToListAsync();

            return Ok(pedidosPendientes);
        }

        // PUT: api/pedidos/{id}/aceptar
        [HttpPut("{id}/aceptar")]
        public async Task<IActionResult> AceptarPedido(Guid id)
        {
            // Verificar si el usuario tiene rol 'repartidor'
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
            {
                return Forbid();
            }

            // Extraer el id_repartidor del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            var idRepartidor = Guid.Parse(userIdClaim.Value);

            var pedido = await _context.Pedidos.FindAsync(id);
            if (pedido == null)
            {
                return NotFound("Pedido no encontrado.");
            }

            // Actualizar el estado del pedido y asignar al repartidor
            pedido.Estado = "en_camino";
            pedido.IdRepartidor = idRepartidor;

            await _context.SaveChangesAsync();

            return Ok();
        }

        // GET: api/pedidos (mis pedidos)
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Pedido>>> GetPedidos()
        {
            // Extraer el id_cliente del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            var idCliente = Guid.Parse(userIdClaim.Value);

            var pedidos = await _context.Pedidos
                .Where(p => p.IdCliente == idCliente)
                .OrderByDescending(p => p.FechaCreado)
                .ToListAsync();

            return Ok(pedidos);
        }

        // PUT: api/pedidos/{id}/asignar-repartidor
        [HttpPut("{id}/asignar-repartidor")]
        public async Task<IActionResult> PutAsignarRepartidor(Guid id)
        {
            // Verificar si el usuario tiene rol 'repartidor'
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
            {
                return Forbid();
            }

            // Extraer el id_repartidor del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            var idRepartidor = Guid.Parse(userIdClaim.Value);

            var pedido = await _context.Pedidos.FindAsync(id);
            if (pedido == null)
            {
                return NotFound("Pedido no encontrado.");
            }

            // Asignar el repartidor y cambiar el estado a 'En Camino'
            pedido.IdRepartidor = idRepartidor;
            pedido.Estado = "En Camino";

            await _context.SaveChangesAsync();

            return NoContent();
        }
    }

    // DTO para la creación de pedidos
    public class CrearPedidoDto
    {
        public Guid IdProducto { get; set; }
        public int Cantidad { get; set; } = 1; // Opcional, por defecto 1
        public double? Latitud { get; set; }
        public double? Longitud { get; set; }
    }
}
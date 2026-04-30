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
                Estado = "pendiente",
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
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
                return Forbid();

            var pedidos = await _context.Pedidos
                .Where(p => p.Estado != null && p.Estado.ToLower() == "pendiente" && p.IdRepartidor == null)
                .OrderByDescending(p => p.FechaCreado)
                .ToListAsync();

            return Ok(pedidos);
        }

        // PUT: api/pedidos/{id}/aceptar
        [HttpPut("{id}/aceptar")]
        public async Task<IActionResult> AceptarPedido(Guid id)
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
                return Forbid();

            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized();

            var idRepartidor = Guid.Parse(userIdClaim.Value);
            var pedido = await _context.Pedidos.FindAsync(id);
            if (pedido == null)
                return NotFound();

            pedido.IdRepartidor = idRepartidor;
            pedido.Estado = "en_camino";
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // PUT: api/pedidos/{id}/entregar
        [HttpPut("{id}/entregar")]
        public async Task<IActionResult> EntregarPedido(Guid id)
        {
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;
            if (userRole != "repartidor")
                return Forbid();

            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized();

            var idRepartidor = Guid.Parse(userIdClaim.Value);
            var pedido = await _context.Pedidos.FindAsync(id);
            if (pedido == null)
                return NotFound();

            if (pedido.IdRepartidor != idRepartidor)
                return Forbid("No estás asignado a este pedido");

            pedido.Estado = "entregado";
            pedido.FechaEntregado = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return NoContent();
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

            // Asignar el repartidor y cambiar el estado a 'en_camino'
            pedido.IdRepartidor = idRepartidor;
            pedido.Estado = "en_camino";

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
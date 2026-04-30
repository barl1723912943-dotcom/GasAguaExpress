using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Data;
using GasAguaAPI.Models;
using System.Security.Claims;

namespace GasAguaAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class RepartidorController : ControllerBase
    {
        private readonly AppDbContext _context;

        public RepartidorController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPut("ubicacion")]
        public async Task<IActionResult> ActualizarUbicacion([FromBody] UbicacionDto dto)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null) return Unauthorized();

            var idUsuario = Guid.Parse(userIdClaim.Value);
            var info = await _context.RepartidoresInfo
                .FirstOrDefaultAsync(r => r.IdUsuario == idUsuario);

            if (info == null) return NotFound();

            info.UltimaLatitud = dto.Latitud;
            info.UltimaLongitud = dto.Longitud;
            info.UltimaActualizacion = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpGet("{pedidoId}/ubicacion-repartidor")]
        public async Task<ActionResult<UbicacionDto>> GetUbicacionRepartidor(Guid pedidoId)
        {
            var pedido = await _context.Pedidos.FindAsync(pedidoId);
            if (pedido?.IdRepartidor == null) return NotFound();

            var info = await _context.RepartidoresInfo
                .FirstOrDefaultAsync(r => r.IdUsuario == pedido.IdRepartidor);

            if (info == null) return NotFound();

            return Ok(new UbicacionDto
            {
                Latitud = info.UltimaLatitud ?? 0,
                Longitud = info.UltimaLongitud ?? 0
            });
        }

        public class UbicacionDto
        {
            public double Latitud { get; set; }
            public double Longitud { get; set; }
        }
    }
}
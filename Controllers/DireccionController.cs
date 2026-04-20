using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Data;
using GasAguaAPI.Models;
using System.Security.Claims;

namespace GasAguaAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DireccionController : ControllerBase
    {
        private readonly AppDbContext _context;

        public DireccionController(AppDbContext context)
        {
            _context = context;
        }

        // POST: api/direcciones
        [HttpPost]
        public async Task<ActionResult<Direccion>> PostDireccion(Direccion direccion)
        {
            // Obtener el ID del usuario del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            // Asociar la dirección al usuario actual
            direccion.IdUsuario = Guid.Parse(userIdClaim.Value);
            direccion.Id = Guid.NewGuid();

            _context.Direcciones.Add(direccion);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetDirecciones), new { id = direccion.Id }, direccion);
        }

        // GET: api/direcciones
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Direccion>>> GetDirecciones()
        {
            // Obtener el ID del usuario del token JWT
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Usuario no autenticado");
            }

            var userId = Guid.Parse(userIdClaim.Value);
            
            var direcciones = await _context.Direcciones
                .Where(d => d.IdUsuario == userId)
                .ToListAsync();

            return Ok(direcciones);
        }
    }
}
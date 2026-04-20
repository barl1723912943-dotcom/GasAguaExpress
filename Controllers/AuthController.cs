using Microsoft.AspNetCore.Mvc;
using GasAguaAPI.Data;
using GasAguaAPI.Models;
using BCrypt.Net;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace GasAguaAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _configuration;

        public AuthController(AppDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request)
        {
            // Validate the input
            if (string.IsNullOrEmpty(request.Nombre) || 
                string.IsNullOrEmpty(request.Telefono) || 
                string.IsNullOrEmpty(request.Contraseña) ||
                string.IsNullOrEmpty(request.Rol))
            {
                return BadRequest(new { Message = "Todos los campos son obligatorios: nombre, telefono, contraseña y rol." });
            }

            // Verify that the role is valid
            var validRoles = new[] { "cliente", "repartidor", "admin" };
            if (!validRoles.Contains(request.Rol.ToLower()))
            {
                return BadRequest(new { Message = "Rol inválido. Debe ser 'cliente', 'repartidor' o 'admin'." });
            }

            // Check if phone number already exists
            var existingUser = _context.Usuarios.FirstOrDefault(u => u.Telefono == request.Telefono);
            if (existingUser != null)
            {
                return BadRequest(new { Message = "El número de teléfono ya está registrado." });
            }

            // Hash the password using BCrypt
            string hashedPassword = BCrypt.Net.BCrypt.HashPassword(request.Contraseña);

            // Create a new user
            var newUser = new Usuario
            {
                Id = Guid.NewGuid(),
                Nombre = request.Nombre,
                Telefono = request.Telefono,
                ContraseñaHash = hashedPassword,
                Rol = request.Rol,
                FechaRegistro = DateTime.UtcNow
            };

            // Add the user to the database
            _context.Usuarios.Add(newUser);
            await _context.SaveChangesAsync();

            // Return success response
            return Ok(new { Message = "Usuario registrado exitosamente." });
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            // Validate input
            if (string.IsNullOrEmpty(request.Telefono) || string.IsNullOrEmpty(request.Contraseña))
            {
                return BadRequest(new { Message = "Teléfono y contraseña son obligatorios." });
            }

            // Find user by phone number
            var user = _context.Usuarios.FirstOrDefault(u => u.Telefono == request.Telefono);
            if (user == null)
            {
                return Unauthorized(new { Message = "Credenciales inválidas." });
            }

            // Verify password using BCrypt
            bool isValidPassword = BCrypt.Net.BCrypt.Verify(request.Contraseña, user.ContraseñaHash);
            if (!isValidPassword)
            {
                return Unauthorized(new { Message = "Credenciales inválidas." });
            }

            // Generate JWT token
            var token = GenerateJwtToken(user);

            // Return token and user info
            return Ok(new
            {
                Token = token,
                User = new
                {
                    Nombre = user.Nombre,
                    Rol = user.Rol
                }
            });
        }

        private string GenerateJwtToken(Usuario user)
        {
            var jwtSettings = _configuration.GetSection("Jwt");
            var keyString = jwtSettings["Key"] ?? throw new ArgumentNullException("JWT Key cannot be null");
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(keyString));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Nombre),
                new Claim(ClaimTypes.Role, user.Rol),
                new Claim("telefono", user.Telefono) // Adding telephone as a custom claim
            };

            var token = new JwtSecurityToken(
                issuer: jwtSettings["Issuer"] ?? "GasAguaAPI",
                audience: jwtSettings["Audience"] ?? "GasAguaApp",
                claims: claims,
                expires: DateTime.Now.AddDays(7), // Token valid for 7 days
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }

    public class RegisterRequest
    {
        public string Nombre { get; set; } = null!;
        public string Telefono { get; set; } = null!;
        public string Contraseña { get; set; } = null!;
        public string Rol { get; set; } = null!;
    }

    public class LoginRequest
    {
        public string Telefono { get; set; } = null!;
        public string Contraseña { get; set; } = null!;
    }
}
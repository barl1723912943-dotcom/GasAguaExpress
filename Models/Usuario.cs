using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Usuarios")]
    public class Usuario
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        [StringLength(255)]
        public string Nombre { get; set; } = null!;
        
        [Required]
        [StringLength(20)]
        public string Telefono { get; set; } = null!;
        
        [Required]
        [StringLength(255)]
        public string ContraseñaHash { get; set; } = null!;
        
        [Required]
        [StringLength(20)]
        public string Rol { get; set; } = null!;
        
        public DateTime FechaRegistro { get; set; }
    }
}
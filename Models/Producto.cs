using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Productos")]
    public class Producto
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        [StringLength(100)]
        public string Nombre { get; set; } = null!;
        
        [StringLength(500)]
        public string? Descripcion { get; set; }
        
        [Required]
        [Column("precio_referencial")]
        public decimal PrecioReferencial { get; set; }
    }
}
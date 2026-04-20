using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Direcciones")]
    public class Direccion
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        [Column("id_usuario")]
        public Guid IdUsuario { get; set; }
        
        [StringLength(100)]
        public string? Sector { get; set; }
        
        [StringLength(255)]
        public string? Calle { get; set; }
        
        [StringLength(255)]
        public string? Referencia { get; set; }
        
        [Column(TypeName = "decimal(9,6)")]
        public decimal? Latitud { get; set; }
        
        [Column(TypeName = "decimal(9,6)")]
        public decimal? Longitud { get; set; }
    }
}
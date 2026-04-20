using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Deudas")]
    public class Deuda
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        public Guid IdRepartidor { get; set; }
        
        [Required]
        public Guid IdPedido { get; set; }
        
        public decimal Monto { get; set; } = 0.20m;
        
        public DateTime Fecha { get; set; }
        
        public bool Pagado { get; set; }
    }
}
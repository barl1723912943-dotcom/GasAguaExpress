using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Pagos")]
    public class Pago
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        public Guid IdRepartidor { get; set; }
        
        [Required]
        public decimal Monto { get; set; }
        
        public DateTime FechaPago { get; set; }
        
        public bool ConfirmadoAdmin { get; set; }
    }
}
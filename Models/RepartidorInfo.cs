using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Repartidores_Info")]
    public class RepartidorInfo
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        public Guid IdUsuario { get; set; }
        
        public bool Disponible { get; set; }
        
        public decimal DeudaTotal { get; set; }
        
        public int TotalEntregas { get; set; }
    }
}
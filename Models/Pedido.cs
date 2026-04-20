using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GasAguaAPI.Models
{
    [Table("Pedidos")]
    public class Pedido
    {
        [Key]
        public Guid Id { get; set; }
        
        [Required]
        [Column("id_cliente")]
        public Guid IdCliente { get; set; }
        
        [Column("id_repartidor")]
        public Guid? IdRepartidor { get; set; }
        
        [Required]
        [Column("id_producto")]
        public Guid IdProducto { get; set; }
        
        [Required]
        [StringLength(20)]
        public string Estado { get; set; } = null!;
        
        [Column("fecha_creado")]
        public DateTime FechaCreado { get; set; }
        
        [Column("fecha_entregado")]
        public DateTime? FechaEntregado { get; set; }
    }
}
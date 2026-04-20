using Microsoft.EntityFrameworkCore;
using GasAguaAPI.Models;

namespace GasAguaAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
        {
        }

        public virtual DbSet<Usuario> Usuarios { get; set; } = null!;
        public virtual DbSet<Direccion> Direcciones { get; set; } = null!;
        public virtual DbSet<Producto> Productos { get; set; } = null!;
        public virtual DbSet<Pedido> Pedidos { get; set; } = null!;
        public virtual DbSet<RepartidorInfo> RepartidoresInfo { get; set; } = null!;
        public virtual DbSet<Deuda> Deudas { get; set; } = null!;
        public virtual DbSet<Pago> Pagos { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Usuario>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.ContraseñaHash).HasColumnName("contraseña_hash");
                entity.Property(e => e.FechaRegistro).HasColumnName("fecha_registro");
                entity.Property(e => e.Rol).HasColumnName("rol");
            });

            modelBuilder.Entity<Direccion>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.IdUsuario).HasColumnName("id_usuario");
                entity.Property(e => e.Latitud).HasColumnType("decimal(9,6)");
                entity.Property(e => e.Longitud).HasColumnType("decimal(9,6)");
            });

            modelBuilder.Entity<Producto>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.PrecioReferencial).HasColumnName("precio_referencial");
            });

            modelBuilder.Entity<Pedido>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.IdCliente).HasColumnName("id_cliente");
                entity.Property(e => e.IdRepartidor).HasColumnName("id_repartidor");
                entity.Property(e => e.IdProducto).HasColumnName("id_producto");
                entity.Property(e => e.FechaCreado).HasColumnName("fecha_creado");
                entity.Property(e => e.FechaEntregado).HasColumnName("fecha_entregado");
            });

            modelBuilder.Entity<RepartidorInfo>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.IdUsuario).HasColumnName("id_usuario");
                entity.Property(e => e.DeudaTotal).HasColumnName("deuda_total");
                entity.Property(e => e.TotalEntregas).HasColumnName("total_entregas");
            });

            modelBuilder.Entity<Deuda>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.IdRepartidor).HasColumnName("id_repartidor");
                entity.Property(e => e.IdPedido).HasColumnName("id_pedido");
            });

            modelBuilder.Entity<Pago>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.IdRepartidor).HasColumnName("id_repartidor");
                entity.Property(e => e.FechaPago).HasColumnName("fecha_pago");
                entity.Property(e => e.ConfirmadoAdmin).HasColumnName("confirmado_admin");
            });
        }
    }
}
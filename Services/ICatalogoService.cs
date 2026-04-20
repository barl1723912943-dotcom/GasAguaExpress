using GasAguaAPI.Models;

namespace GasAguaAPI.Services
{
    public interface ICatalogoService
    {
        Task ResetearCatalogoAsync();
        Task<Producto?> GetProductoByIdAsync(Guid id);
    }
}
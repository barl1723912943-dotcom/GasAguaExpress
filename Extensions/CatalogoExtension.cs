using GasAguaAPI.Data;
using GasAguaAPI.Services;

namespace GasAguaAPI.Extensions
{
    public static class CatalogoExtension
    {
        /// <summary>
        /// Método para inicializar el catálogo de productos con los datos reales de El Empalme
        /// </summary>
        /// <param name="app"></param>
        public static async Task InicializarCatalogo(this WebApplication app)
        {
            using var scope = app.Services.CreateScope();
            var catalogService = scope.ServiceProvider.GetRequiredService<CatalogoService>();
            await catalogService.ResetearCatalogoAsync();
        }
    }
}
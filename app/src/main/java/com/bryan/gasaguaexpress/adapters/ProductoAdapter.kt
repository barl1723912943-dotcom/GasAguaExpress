package com.bryan.gasaguaexpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bryan.gasaguaexpress.R
import com.bryan.gasaguaexpress.models.Producto

class ProductoAdapter(private var productos: List<Producto>) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {
    // Esta variable almacenará la función que se ejecutará al hacer clic en un producto
    var onItemClickListener: ((Producto) -> Unit)? = null

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreProducto: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvPrecioProducto: TextView = itemView.findViewById(R.id.tvPrecioProducto)
        val btnPedir: Button = itemView.findViewById(R.id.btnPedir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        
        holder.tvNombreProducto.text = producto.nombre
        // Formateamos el precio para que se vea como moneda (ej: $3.00)
        holder.tvPrecioProducto.text = String.format("$%.2f", producto.precio)

        holder.btnPedir.setOnClickListener {
            onItemClickListener?.invoke(producto)
        }
    }

    override fun getItemCount(): Int = productos.size

    // Esta función es vital para actualizar la lista cuando lleguen los datos del backend
    fun updateData(newProductos: List<Producto>) {
        productos = newProductos
        notifyDataSetChanged()
    }
}
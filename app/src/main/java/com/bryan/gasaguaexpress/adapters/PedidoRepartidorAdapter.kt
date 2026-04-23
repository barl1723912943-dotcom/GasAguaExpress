package com.bryan.gasaguaexpress.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bryan.gasaguaexpress.models.PedidoResponse

import com.bryan.gasaguaexpress.databinding.ItemPedidoRepartidorBinding


class PedidoRepartidorAdapter(
    private var pedidos: List<PedidoResponse>,
    private val onAceptarClick: (PedidoResponse) -> Unit
) : RecyclerView.Adapter<PedidoRepartidorAdapter.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding =
            ItemPedidoRepartidorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.bind(pedido)
        // Usamos el botón desde la función bind, o pasamos el click listener al holder
        holder.itemView.setOnClickListener {
            // Opcional: si quieres que al tocar toda la tarjeta pase algo
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun updateData(newPedidos: List<PedidoResponse>) {
        pedidos = newPedidos
        notifyDataSetChanged()
    }

    inner class PedidoViewHolder(private val binding: ItemPedidoRepartidorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pedido: PedidoResponse) {
            binding.tvIdPedido.text = "ID: ${pedido.id}"
            // Usamos fecha_creado (o como se llame en tu modelo), no fechaEntregado porque el pedido es nuevo
            binding.tvFecha.text = "Estado: ${pedido.estado}"

            binding.btnAceptarPedido.setOnClickListener {
                onAceptarClick(pedido)
            }
        }
    }
}
package com.example.agricultural

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.agricultural.databinding.ItemFarmBinding

class FarmAdapter(private val onClick: (Farm) -> Unit) :
    ListAdapter<Farm, FarmAdapter.VH>(Diff) {

    class VH(val b: ItemFarmBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFarmBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val farm = getItem(position)
        holder.b.tvFarmName.text = farm.farmName
        holder.b.tvFarmMeta.text = "${farm.farmCode} · ${farm.ownerName}"
        holder.b.tvFarmAddress.text = farm.farmAddress
        holder.b.root.setOnClickListener { onClick(farm) }
    }

    object Diff : DiffUtil.ItemCallback<Farm>() {
        override fun areItemsTheSame(a: Farm, b: Farm) = a.id == b.id
        override fun areContentsTheSame(a: Farm, b: Farm) = a == b
    }
}
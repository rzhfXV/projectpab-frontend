package com.kel6.booking.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kel6.booking.R
import com.kel6.booking.data.model.CourtResponse
import com.kel6.booking.databinding.ItemCourtBinding
import com.kel6.booking.utils.toRupiah

class CourtAdapter(
    private val onItemClick: (CourtResponse) -> Unit
) : ListAdapter<CourtResponse, CourtAdapter.CourtViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val binding = ItemCourtBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourtViewHolder(
        private val binding: ItemCourtBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(court: CourtResponse) {
            binding.tvCourtName.text  = court.name
            binding.tvDescription.text = court.description ?: "Lapangan padel berkualitas"
            binding.tvPrice.text      = "${court.pricePerHour.toRupiah()}/jam"

            // Load foto lapangan
            if (!court.photoUrl.isNullOrEmpty()) {
                val baseUrl = binding.root.context.getString(R.string.base_url)
                Glide.with(binding.root.context)
                    .load("${baseUrl}api/files/${court.photoUrl}")
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(binding.ivCourtPhoto)
            }

            binding.root.setOnClickListener { onItemClick(court) }
            binding.btnBook.setOnClickListener { onItemClick(court) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CourtResponse>() {
        override fun areItemsTheSame(a: CourtResponse, b: CourtResponse) = a.id == b.id
        override fun areContentsTheSame(a: CourtResponse, b: CourtResponse) = a == b
    }
}
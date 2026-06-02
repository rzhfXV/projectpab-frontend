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
    private val onClick: (CourtResponse) -> Unit
) : ListAdapter<CourtResponse, CourtAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemCourtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(court: CourtResponse) {
            binding.tvCourtName.text = court.name
            binding.tvPrice.text = "${court.pricePerHour.toRupiah()} / jam"
            binding.tvDescription.text = court.description ?: "Lapangan padel berkualitas"

            // Jadwal hari operasional
            val dayCount = court.schedules?.size ?: 0
            binding.tvSchedule.text = if (dayCount > 0) "$dayCount hari/minggu" else "Hubungi kami"

            // Load foto lapangan
            Glide.with(binding.root)
                .load(court.photoUrl)
                .placeholder(R.drawable.placeholder_court)
                .error(R.drawable.placeholder_court)
                .centerCrop()
                .into(binding.ivCourt)

            binding.root.setOnClickListener { onClick(court) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCourtBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CourtResponse>() {
        override fun areItemsTheSame(a: CourtResponse, b: CourtResponse) = a.id == b.id
        override fun areContentsTheSame(a: CourtResponse, b: CourtResponse) = a == b
    }
}
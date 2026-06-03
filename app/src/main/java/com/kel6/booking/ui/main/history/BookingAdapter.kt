package com.kel6.booking.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.R
import com.kel6.booking.data.model.BookingResponse
import com.kel6.booking.databinding.ItemBookingBinding
import com.kel6.booking.utils.bookingStatusColor
import com.kel6.booking.utils.bookingStatusLabel
import com.kel6.booking.utils.toDisplayDate
import com.kel6.booking.utils.toDisplayTime
import com.kel6.booking.utils.toRupiah

class BookingAdapter(
    private val onItemClick: (BookingResponse) -> Unit
) : ListAdapter<BookingResponse, BookingAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBookingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: BookingResponse) {
            binding.tvCourtName.text = booking.courtName
            binding.tvDate.text      = booking.bookingDate.toDisplayDate()
            binding.tvTime.text      =
                "${booking.startTime.toDisplayTime()} – ${booking.endTime.toDisplayTime()} " +
                        "(${booking.durationHours.toInt()} jam)"
            binding.tvPrice.text     = booking.totalPrice.toRupiah()

            // Chip status
            val statusLabel = booking.status.bookingStatusLabel()
            val statusColor = booking.status.bookingStatusColor()
            binding.chipStatus.text = statusLabel
            binding.chipStatus.setChipBackgroundColorResource(
                when (booking.status) {
                    "CONFIRMED" -> R.color.status_confirmed
                    "PENDING"   -> R.color.status_pending
                    "WAITING_PAYMENT" -> R.color.status_waiting
                    "REJECTED", "CANCELLED" -> R.color.status_rejected
                    else -> R.color.status_done
                }
            )
            binding.chipStatus.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.white)
            )

            // Strip warna di kiri sesuai status
            binding.viewStatus.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BookingResponse>() {
        override fun areItemsTheSame(a: BookingResponse, b: BookingResponse) = a.id == b.id
        override fun areContentsTheSame(a: BookingResponse, b: BookingResponse) = a == b
    }
}
package com.kel6.booking.ui.main.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.data.model.NotificationResponse
import com.kel6.booking.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Locale

// ── NotifAdapter ───────────────────────────────────────────────
class NotifAdapter(
    private val onItemClick: (NotificationResponse) -> Unit
) : ListAdapter<NotificationResponse, NotifAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notif: NotificationResponse) {
            binding.tvTitle.text   = notif.title
            binding.tvMessage.text = notif.message
            binding.tvTime.text    = formatTime(notif.createdAt)

            // Dot belum dibaca
            binding.dotUnread.visibility =
                if (!notif.read) android.view.View.VISIBLE else android.view.View.INVISIBLE

            // Background lebih terang kalau belum dibaca
            binding.root.setCardBackgroundColor(
                if (!notif.read)
                    android.graphics.Color.parseColor("#EFF6FF")
                else
                    android.graphics.Color.WHITE
            )

            binding.root.setOnClickListener { onItemClick(notif) }
        }

        private fun formatTime(dateStr: String): String {
            return try {
                val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("dd MMM, HH:mm", Locale("id"))
                output.format(input.parse(dateStr)!!)
            } catch (e: Exception) { dateStr }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationResponse>() {
        override fun areItemsTheSame(a: NotificationResponse, b: NotificationResponse) = a.id == b.id
        override fun areContentsTheSame(a: NotificationResponse, b: NotificationResponse) = a == b
    }
}
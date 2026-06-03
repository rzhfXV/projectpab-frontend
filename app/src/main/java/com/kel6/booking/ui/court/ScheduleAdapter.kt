package com.kel6.booking.ui.court

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.data.model.ScheduleInfo
import com.kel6.booking.databinding.ItemScheduleBinding

// ── ScheduleAdapter ──────────────────────────────────────────
class ScheduleAdapter(
    private val schedules: List<ScheduleInfo>
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount() = schedules.size

    class ViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleInfo) {
            binding.tvDay.text   = schedule.dayName
            binding.tvHours.text = "${schedule.openTime} – ${schedule.closeTime}"
        }
    }
}
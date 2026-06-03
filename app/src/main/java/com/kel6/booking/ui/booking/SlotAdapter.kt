package com.kel6.booking.ui.booking

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.data.model.SlotResponse
import com.kel6.booking.databinding.ItemSlotBinding

class SlotAdapter(
    private val onSlotRangeSelected: (startTime: String, endTime: String) -> Unit
) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {

    private val slots = mutableListOf<SlotResponse>()
    private var selectedStartIndex = -1
    private var selectedEndIndex   = -1
    private var adapterContext: android.content.Context? = null

    fun submitSlots(newSlots: List<SlotResponse>) {
        slots.clear()
        slots.addAll(newSlots)
        selectedStartIndex = -1
        selectedEndIndex   = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        adapterContext = parent.context
        val binding = ItemSlotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(slots[position], position)
    }

    override fun getItemCount() = slots.size

    inner class ViewHolder(private val binding: ItemSlotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: SlotResponse, position: Int) {
            binding.tvSlotTime.text = slot.startTime.substring(0, 5)

            val isSelected = position in selectedStartIndex..selectedEndIndex

            when {
                !slot.available -> {
                    // Slot terisi — merah, tidak bisa diklik
                    binding.root.setCardBackgroundColor(Color.parseColor("#FDECEA"))
                    binding.root.strokeColor = Color.parseColor("#E74C3C")
                    binding.tvSlotTime.setTextColor(Color.parseColor("#E74C3C"))
                    binding.root.isClickable = false
                    binding.root.alpha = 0.7f
                }
                isSelected -> {
                    // Slot dipilih — biru tua
                    binding.root.setCardBackgroundColor(Color.parseColor("#1B4F72"))
                    binding.root.strokeColor = Color.parseColor("#1B4F72")
                    binding.tvSlotTime.setTextColor(Color.WHITE)
                    binding.root.isClickable = true
                    binding.root.alpha = 1f
                }
                else -> {
                    // Slot tersedia — hijau muda
                    binding.root.setCardBackgroundColor(Color.parseColor("#E8F8F0"))
                    binding.root.strokeColor = Color.parseColor("#27AE60")
                    binding.tvSlotTime.setTextColor(Color.parseColor("#1A5C35"))
                    binding.root.isClickable = true
                    binding.root.alpha = 1f
                }
            }

            binding.root.setOnClickListener {
                if (!slot.available) return@setOnClickListener
                handleSlotTap(position)
            }
        }
    }

    private fun handleSlotTap(position: Int) {
        when {
            // Belum ada yang dipilih → set sebagai start
            selectedStartIndex == -1 -> {
                selectedStartIndex = position
                selectedEndIndex   = position
            }
            // Tap slot yang sama → reset
            position == selectedStartIndex && selectedStartIndex == selectedEndIndex -> {
                selectedStartIndex = -1
                selectedEndIndex   = -1
            }
            // Tap slot setelah start → set sebagai end (kalau semua di antaranya tersedia)
            position > selectedStartIndex -> {
                // Validasi tidak ada slot terisi di antara start dan end
                val allAvailable = (selectedStartIndex..position).all { slots[it].available }
                if (allAvailable) {
                    selectedEndIndex = position
                } else {
                    showToast("Ada slot yang sudah terisi di rentang waktu ini")
                    return
                }
            }
            // Tap sebelum start → jadikan start baru
            else -> {
                selectedStartIndex = position
                selectedEndIndex   = position
            }
        }

        notifyDataSetChanged()

        // Kirim callback kalau sudah ada range yang dipilih
        if (selectedStartIndex != -1) {
            val startTime = slots[selectedStartIndex].startTime.substring(0, 5)
            val endTime   = slots[selectedEndIndex].endTime.substring(0, 5)
            onSlotRangeSelected(startTime, endTime)
        } else {
            onSlotRangeSelected("", "")
        }
    }

    private fun showToast(msg: String) {
        adapterContext?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
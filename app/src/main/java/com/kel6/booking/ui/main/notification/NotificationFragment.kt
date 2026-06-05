package com.kel6.booking.ui.main.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.data.model.NotificationResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.FragmentNotificationBinding
import com.kel6.booking.databinding.ItemNotificationBinding
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotifAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotifAdapter { notif ->
            // Tap notif → tandai dibaca
            if (!notif.read) {
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(requireContext()).markAsRead(notif.id)
                        loadNotifications()
                    } catch (_: Exception) {}
                }
            }
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@NotificationFragment.adapter
        }

        binding.btnReadAll.setOnClickListener {
            lifecycleScope.launch {
                try {
                    RetrofitClient.getInstance(requireContext()).markAllAsRead()
                    loadNotifications()
                    toast("Semua notifikasi ditandai dibaca")
                } catch (_: Exception) {}
            }
        }

        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        binding.progressBar.show()
        binding.layoutEmpty.hide()

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.getInstance(requireContext()).getNotifications()
                if (res.isSuccessful) {
                    val list = res.body()?.data ?: emptyList()
                    if (list.isEmpty()) {
                        binding.layoutEmpty.show()
                        binding.rvNotifications.hide()
                    } else {
                        adapter.submitList(list)
                        binding.rvNotifications.show()
                        binding.layoutEmpty.hide()
                    }
                } else {
                    // Cek apakah error dari backend LazyInitializationException
                    val errBody = res.errorBody()?.string() ?: ""
                    if (res.code() == 500 && errBody.contains("could not initialize proxy")) {
                        toast("Server error: relasi data belum dimuat. Coba lagi.")
                    } else {
                        toast("Gagal memuat notifikasi (${res.code()})")
                    }
                    binding.layoutEmpty.show()
                    binding.rvNotifications.hide()
                }
            } catch (e: Exception) {
                toast("Gagal memuat notifikasi")
                binding.layoutEmpty.show()
                binding.rvNotifications.hide()
            } finally {
                binding.progressBar.hide()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


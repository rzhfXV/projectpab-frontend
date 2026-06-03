package com.kel6.booking.ui.main.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.R
import com.kel6.booking.data.model.BookingResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.FragmentHistoryBinding
import com.kel6.booking.databinding.ItemBookingBinding
import com.kel6.booking.ui.booking.BookingDetailActivity
import com.kel6.booking.utils.*
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadBookings()
    }

    override fun onResume() {
        super.onResume()
        loadBookings() // refresh tiap kali kembali ke tab ini
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter { booking ->
            val intent = Intent(requireContext(), BookingDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.id)
            startActivity(intent)
        }
        binding.rvBookings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookingAdapter
        }
    }

    private fun loadBookings() {
        binding.progressBar.show()
        binding.layoutEmpty.hide()
        binding.rvBookings.hide()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(requireContext()).getMyBookings()
                if (response.isSuccessful && response.body()?.success == true) {
                    val bookings = response.body()!!.data ?: emptyList()
                    if (bookings.isEmpty()) {
                        binding.layoutEmpty.show()
                    } else {
                        bookingAdapter.submitList(bookings)
                        binding.rvBookings.show()
                    }
                } else {
                    val errorBodyStr = response.errorBody()?.string() ?: ""
                    
                    // Deteksi bug LazyInitializationException dari Backend
                    val msg = if (response.code() == 500 && errorBodyStr.contains("could not initialize proxy")) {
                        "Gagal memuat riwayat: Bug di server (LazyInitializationException). Hubungi admin server."
                    } else {
                        response.body()?.message ?: try {
                            org.json.JSONObject(errorBodyStr).optString("message", "")
                                .ifEmpty { "Error ${response.code()}: Gagal memuat riwayat" }
                        } catch (_: Exception) { "Error ${response.code()}: Gagal memuat riwayat" }
                    }
                    
                    toast(msg)
                    binding.layoutEmpty.show()
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server. Cek koneksi dan IP backend.")
                binding.layoutEmpty.show()
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
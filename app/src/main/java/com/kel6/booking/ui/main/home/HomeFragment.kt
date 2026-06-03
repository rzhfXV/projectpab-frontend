package com.kel6.booking.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kel6.booking.data.model.CourtResponse
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.FragmentHomeBinding
import com.kel6.booking.ui.court.CourtDetailActivity
import com.kel6.booking.ui.main.notification.NotificationFragment
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.toast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var courtAdapter: CourtAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadUserName()
        loadCourts()

        // Tap ikon notifikasi → pindah ke tab notifikasi
        binding.ivNotif.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.kel6.booking.R.id.bottomNav
            ).selectedItemId = com.kel6.booking.R.id.notificationFragment
        }
    }

    private fun setupRecyclerView() {
        courtAdapter = CourtAdapter { court ->
            // Tap card → buka CourtDetailActivity
            requireContext().let { ctx ->
                val intent = android.content.Intent(ctx, CourtDetailActivity::class.java)
                intent.putExtra(Constants.EXTRA_COURT_ID, court.id)
                intent.putExtra(Constants.EXTRA_COURT_NAME, court.name)
                startActivity(intent)
            }
        }
        binding.rvCourts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courtAdapter
        }
    }

    private fun loadUserName() {
        lifecycleScope.launch {
            val name = UserPreferences(requireContext()).userName.first()
            binding.tvGreeting.text = "Halo, ${name ?: "Pengguna"} 👋"
        }
    }

    private fun loadCourts() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(requireContext()).getCourts()
                if (response.isSuccessful && response.body()?.success == true) {
                    val courts = response.body()!!.data ?: emptyList()
                    if (courts.isEmpty()) {
                        showEmpty()
                    } else {
                        courtAdapter.submitList(courts)
                        showData()
                    }
                } else {
                    toast("Gagal memuat lapangan")
                    showEmpty()
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
                showEmpty()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.shimmerLayout.show()
            binding.shimmerLayout.startShimmer()
            binding.rvCourts.hide()
            binding.layoutEmpty.hide()
        }
    }

    private fun showData() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.hide()
        binding.rvCourts.show()
        binding.layoutEmpty.hide()
    }

    private fun showEmpty() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.hide()
        binding.rvCourts.hide()
        binding.layoutEmpty.show()
    }

    override fun onResume() {
        super.onResume()
        loadNotifBadge()
    }

    private fun loadNotifBadge() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.getInstance(requireContext()).getUnreadCount()
                val count = res.body()?.data?.count ?: 0
                // Tampilkan angka di ikon notif kalau ada yang belum dibaca
                binding.ivNotif.alpha = if (count > 0) 1f else 0.6f
            } catch (_: Exception) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
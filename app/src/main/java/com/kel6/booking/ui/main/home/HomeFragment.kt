package com.kel6.booking.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kel6.booking.data.model.CourtResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.FragmentHomeBinding
import com.kel6.booking.ui.court.CourtDetailActivity
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.startActivity
import com.kel6.booking.utils.toast
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourtAdapter

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
        loadCourts()

        binding.swipeRefresh.setOnRefreshListener { loadCourts() }
    }

    private fun setupRecyclerView() {
        adapter = CourtAdapter { court ->
            requireContext().startActivity<CourtDetailActivity> {
                putExtra(Constants.EXTRA_COURT_ID, court.id)
                putExtra(Constants.EXTRA_COURT_NAME, court.name)
            }
        }
        binding.rvCourts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourts.adapter = adapter
    }

    private fun loadCourts() {
        showShimmer(true)
        binding.layoutError.hide()

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getInstance(requireContext())
                val response = api.getCourts()

                if (response.isSuccessful && response.body()?.success == true) {
                    val courts = response.body()!!.data ?: emptyList()
                    if (courts.isEmpty()) {
                        showEmpty(true)
                    } else {
                        showEmpty(false)
                        adapter.submitList(courts)
                    }
                } else {
                    showError("Gagal memuat data lapangan")
                }
            } catch (e: Exception) {
                showError("Tidak dapat terhubung ke server")
            } finally {
                showShimmer(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerLayout.startShimmer()
            binding.shimmerLayout.show()
            binding.rvCourts.hide()
        } else {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.hide()
            binding.rvCourts.show()
        }
    }

    private fun showEmpty(show: Boolean) {
        if (show) binding.layoutEmpty.show() else binding.layoutEmpty.hide()
    }

    private fun showError(msg: String) {
        binding.layoutError.show()
        binding.tvError.text = msg
        binding.btnRetry.setOnClickListener { loadCourts() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
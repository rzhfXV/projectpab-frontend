package com.kel6.booking.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.databinding.FragmentProfileBinding
import com.kel6.booking.ui.auth.LoginActivity
import com.kel6.booking.utils.startActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Keluar") { _, _ -> doLogout() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val prefs = UserPreferences(requireContext())
            binding.tvName.text  = prefs.userName.first() ?: "-"
            binding.tvEmail.text = prefs.userRole.first()?.let { role ->
                // ambil email dari DataStore — tambahkan KEY_EMAIL jika perlu
                "-"
            } ?: "-"
            binding.tvPhone.text = "-"
            binding.chipRole.text = prefs.userRole.first() ?: "USER"
        }
    }

    private fun doLogout() {
        lifecycleScope.launch {
            UserPreferences(requireContext()).clearSession()
            requireContext().startActivity<LoginActivity>(clearStack = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
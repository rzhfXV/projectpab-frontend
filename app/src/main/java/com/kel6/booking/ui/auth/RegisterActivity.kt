package com.kel6.booking.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.data.model.RegisterRequest
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityRegisterBinding
import com.kel6.booking.ui.main.MainActivity
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.startActivity
import com.kel6.booking.utils.toast
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = UserPreferences(this)

        binding.btnBack.setOnClickListener  { finish() }
        binding.tvLogin.setOnClickListener  { finish() }
        binding.btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val name     = binding.etName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val phone    = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validasi
        var valid = true
        if (name.isEmpty()) {
            binding.tilName.error = "Nama tidak boleh kosong"; valid = false
        } else binding.tilName.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"; valid = false
        } else binding.tilEmail.error = null

        if (phone.isEmpty() || phone.length < 10) {
            binding.tilPhone.error = "Nomor HP tidak valid"; valid = false
        } else binding.tilPhone.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"; valid = false
        } else binding.tilPassword.error = null

        if (!valid) return

        setLoading(true)

        lifecycleScope.launch {
            try {
                val api      = RetrofitClient.getInstance(this@RegisterActivity)
                val response = api.register(RegisterRequest(name, email, password, phone))

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    prefs.saveUserSession(
                        token  = data.token,
                        userId = data.user.id,
                        name   = data.user.name,
                        email  = data.user.email,
                        phone  = data.user.phone,
                        role   = data.user.role
                    )
                    toast("Registrasi berhasil! Selamat datang, ${data.user.name}!")
                    startActivity<MainActivity>(clearStack = true)
                } else {
                    toast(response.body()?.message ?: "Registrasi gagal")
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server. Cek koneksi.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.progressBar.show()
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text      = "Memproses..."
        } else {
            binding.progressBar.hide()
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text      = "Daftar"
        }
    }
}
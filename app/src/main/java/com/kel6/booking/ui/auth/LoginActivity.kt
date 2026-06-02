package com.kel6.booking.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.data.model.LoginRequest
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityLoginBinding
import com.kel6.booking.ui.main.MainActivity
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.startActivity
import com.kel6.booking.utils.toast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = UserPreferences(this)

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }

    private fun doLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validasi input
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }
        binding.tilEmail.error    = null
        binding.tilPassword.error = null

        setLoading(true)

        lifecycleScope.launch {
            try {
                val api      = RetrofitClient.getInstance(this@LoginActivity)
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    // Simpan token dan info user ke DataStore
                    prefs.saveUserSession(
                        token  = data.token,
                        userId = data.user.id,
                        name   = data.user.name,
                        email  = data.user.email,
                        phone  = data.user.phone,
                        role   = data.user.role
                    )
                    toast("Selamat datang, ${data.user.name}!")
                    startActivity<MainActivity>(clearStack = true)
                } else {
                    val msg = response.body()?.message ?: "Login gagal"
                    toast(msg)
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
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text      = "Memproses..."
        } else {
            binding.progressBar.hide()
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text      = "Login"
        }
    }
}
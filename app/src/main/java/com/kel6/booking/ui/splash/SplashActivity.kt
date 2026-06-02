package com.kel6.booking.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.databinding.ActivitySplashBinding
import com.kel6.booking.ui.auth.LoginActivity
import com.kel6.booking.ui.main.MainActivity
import com.kel6.booking.utils.startActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(1500) // tampilkan splash 1.5 detik

            val isLoggedIn = UserPreferences(this@SplashActivity).isLoggedIn.first()

            if (isLoggedIn) {
                // Sudah punya token → langsung ke MainActivity
                startActivity<MainActivity>(clearStack = true)
            } else {
                // Belum login → ke LoginActivity
                startActivity<LoginActivity>(clearStack = true)
            }
        }
    }
}
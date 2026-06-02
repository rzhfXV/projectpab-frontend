package com.kel6.booking.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kel6.booking.R
import com.kel6.booking.data.preferences.UserPreferences
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityMainBinding
import com.google.android.material.badge.BadgeDrawable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var notifBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        // Badge pada tab Notifikasi
        notifBadge = binding.bottomNav.getOrCreateBadge(R.id.notificationFragment).apply {
            isVisible = false
        }

        startUnreadPolling()
    }

    /** Poll unread count setiap 30 detik selama activity hidup */
    private fun startUnreadPolling() {
        lifecycleScope.launch {
            while (isActive) {
                refreshUnreadBadge()
                delay(30_000)
            }
        }
    }

    private suspend fun refreshUnreadBadge() {
        try {
            val api = RetrofitClient.getInstance(this)
            val resp = api.getUnreadCount()
            if (resp.isSuccessful) {
                val count = resp.body()?.data?.count ?: 0L
                notifBadge?.apply {
                    if (count > 0) {
                        number = count.toInt()
                        isVisible = true
                    } else {
                        isVisible = false
                    }
                }
            }
        } catch (_: Exception) { /* abaikan error polling */ }
    }

    /** Dipanggil dari NotificationFragment setelah user membaca notif */
    fun clearNotifBadge() {
        notifBadge?.isVisible = false
    }
}
package com.kel6.booking.ui.court

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kel6.booking.R
import com.kel6.booking.data.model.CourtResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityCourtDetailBinding
import com.kel6.booking.databinding.ItemScheduleBinding
import com.kel6.booking.ui.booking.BookingActivity
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.toast
import com.kel6.booking.utils.toRupiah
import kotlinx.coroutines.launch

class CourtDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourtDetailBinding
    private var courtId: Long = 0
    private var courtName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourtDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courtId   = intent.getLongExtra(Constants.EXTRA_COURT_ID, 0)
        courtName = intent.getStringExtra(Constants.EXTRA_COURT_NAME) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadCourtDetail()

        binding.btnBookNow.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("price_per_hour", court.pricePerHour)
            intent.putExtra(Constants.EXTRA_COURT_ID, courtId)
            intent.putExtra(Constants.EXTRA_COURT_NAME, courtName)
            startActivity(intent)
        }
    }

    private fun loadCourtDetail() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@CourtDetailActivity)
                    .getCourtById(courtId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val court = response.body()!!.data!!
                    bindData(court)
                } else {
                    toast("Gagal memuat detail lapangan")
                    finish()
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
                finish()
            }
        }
    }

    private fun bindData(court: CourtResponse) {
        // Toolbar title
        binding.toolbar.title = court.name

        // Foto
        if (!court.photoUrl.isNullOrEmpty()) {
            val baseUrl = getString(R.string.base_url)
            Glide.with(this)
                .load("${baseUrl}api/files/${court.photoUrl}")
                .centerCrop()
                .into(binding.ivCourtPhoto)
        }

        binding.tvCourtName.text  = court.name
        binding.tvDescription.text = court.description ?: "Lapangan padel berkualitas"
        binding.tvPrice.text      = court.pricePerHour.toRupiah()

        // Jadwal operasional
        val schedules = court.schedules ?: emptyList()
        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = ScheduleAdapter(schedules)
    }
}

package com.kel6.booking.ui.booking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.data.model.BookingResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityBookingDetailBinding
import com.kel6.booking.ui.payment.PaymentActivity
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.toast
import com.kel6.booking.utils.toDisplayDate
import com.kel6.booking.utils.toDisplayTime
import com.kel6.booking.utils.toRupiah
import com.kel6.booking.utils.bookingStatusLabel
import com.kel6.booking.utils.bookingStatusColor
import kotlinx.coroutines.launch

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private var bookingId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingId = intent.getLongExtra(Constants.EXTRA_BOOKING_ID, 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadBookingDetail()

        binding.btnPayNow.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId)
            startActivity(intent)
        }

        binding.btnCancel.setOnClickListener {
            showCancelConfirmation()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from PaymentActivity
        loadBookingDetail()
    }

    private fun loadBookingDetail() {
        binding.progressBar.show()
        binding.cardStatus.hide()
        binding.cardDetail.hide()
        binding.cardPayment.hide()
        binding.cardNotes.hide()
        binding.layoutActions.hide()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@BookingDetailActivity)
                    .getBookingById(bookingId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val booking = response.body()!!.data!!
                    bindData(booking)
                } else {
                    val errorBodyStr = response.errorBody()?.string() ?: ""
                    
                    val errMsg = if (response.code() == 500 && errorBodyStr.contains("could not initialize proxy")) {
                        "Gagal memuat detail booking: Bug di server (LazyInitializationException)."
                    } else {
                        response.body()?.message ?: try {
                            org.json.JSONObject(errorBodyStr).optString("message", "")
                                .ifEmpty { "Gagal memuat detail booking" }
                        } catch (_: Exception) { "Gagal memuat detail booking" }
                    }
                    toast(errMsg)
                    finish()
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
                finish()
            } finally {
                binding.progressBar.hide()
            }
        }
    }

    private fun bindData(booking: BookingResponse) {
        // Status card
        binding.cardStatus.show()
        binding.tvStatus.text = booking.status.bookingStatusLabel()
        binding.tvStatus.setTextColor(
            getColor(booking.status.bookingStatusColor())
        )

        // Detail card
        binding.cardDetail.show()
        binding.tvCourtName.text = booking.courtName
        binding.tvDate.text      = booking.bookingDate.toDisplayDate()
        binding.tvTime.text      = "${booking.startTime.toDisplayTime()} – ${booking.endTime.toDisplayTime()}"
        binding.tvDuration.text  = "${booking.durationHours.toInt()} jam"
        binding.tvTotalPrice.text = booking.totalPrice.toRupiah()

        // Payment info
        if (!booking.paymentMethod.isNullOrEmpty()) {
            binding.cardPayment.show()
            binding.tvPaymentMethod.text  = booking.paymentMethod
            binding.tvPaymentStatus.text  = booking.paymentStatus ?: "-"
        }

        // Notes
        if (!booking.notes.isNullOrEmpty()) {
            binding.cardNotes.show()
            binding.tvNotes.text = booking.notes
        }

        // Action buttons
        binding.layoutActions.show()
        when (booking.status) {
            "PENDING" -> {
                binding.btnPayNow.show()
                binding.btnCancel.show()
            }
            "WAITING_PAYMENT" -> {
                if (booking.paymentStatus == "UPLOADED") {
                    // Jika sudah upload bukti, override status text dan sembunyikan tombol upload
                    binding.tvStatus.text = "Menunggu Verifikasi Admin"
                    binding.btnPayNow.hide()
                    binding.btnCancel.show()
                } else {
                    binding.btnPayNow.show()
                    binding.btnPayNow.text = "Upload Bukti"
                    binding.btnCancel.show()
                }
            }
            "CONFIRMED" -> {
                binding.btnPayNow.hide()
                binding.btnCancel.hide()
                binding.layoutActions.hide()
            }
            else -> {
                binding.btnPayNow.hide()
                binding.btnCancel.hide()
                binding.layoutActions.hide()
            }
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Batalkan Booking")
            .setMessage("Yakin ingin membatalkan booking ini?")
            .setPositiveButton("Ya, Batalkan") { _, _ -> cancelBooking() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun cancelBooking() {
        binding.btnCancel.isEnabled = false
        binding.btnCancel.text = "Memproses..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@BookingDetailActivity)
                    .cancelBooking(bookingId)

                if (response.isSuccessful && response.body()?.success == true) {
                    toast("Booking berhasil dibatalkan")
                    loadBookingDetail()
                } else {
                    toast(response.body()?.message ?: "Gagal membatalkan booking")
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
            } finally {
                binding.btnCancel.isEnabled = true
                binding.btnCancel.text = "Batalkan Booking"
            }
        }
    }
}

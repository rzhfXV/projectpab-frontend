package com.kel6.booking.ui.booking

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kel6.booking.data.model.BookingRequest
import com.kel6.booking.data.model.SlotResponse
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityBookingBinding
import com.kel6.booking.databinding.ItemSlotBinding
import com.kel6.booking.ui.payment.PaymentActivity
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.toast
import com.kel6.booking.utils.toDisplayDate
import com.kel6.booking.utils.toRupiah
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private lateinit var slotAdapter: SlotAdapter

    private var courtId: Long = 0
    private var courtName: String = ""
    private var pricePerHour: Double = 0.0

    private var selectedDate: String = ""       // format: "2025-07-01"
    private var selectedStartTime: String = ""  // format: "08:00"
    private var selectedEndTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courtId   = intent.getLongExtra(Constants.EXTRA_COURT_ID, 0)
        courtName = intent.getStringExtra(Constants.EXTRA_COURT_NAME) ?: ""
        pricePerHour = intent.getDoubleExtra("price_per_hour", 0.0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvCourtName.text = courtName

        setupSlotRecyclerView()

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnConfirmBooking.setOnClickListener { confirmBooking() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        // Minimal booking besok
        cal.add(Calendar.DAY_OF_MONTH, 1)

        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
                binding.btnPickDate.text = selectedDate.toDisplayDate()
                // Reset pilihan slot saat tanggal berubah
                selectedStartTime = ""
                selectedEndTime   = ""
                updateSummary()
                loadSlots()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        // Tanggal minimum: besok
        picker.datePicker.minDate = cal.timeInMillis
        picker.show()
    }

    private fun setupSlotRecyclerView() {
        slotAdapter = SlotAdapter { startTime, endTime ->
            selectedStartTime = startTime
            selectedEndTime   = endTime
            updateSummary()
        }
        binding.rvSlots.apply {
            layoutManager = GridLayoutManager(this@BookingActivity, 3)
            adapter = slotAdapter
        }
    }

    private fun loadSlots() {
        if (selectedDate.isEmpty()) return

        binding.cardSlots.show()
        binding.progressSlots.show()
        binding.rvSlots.hide()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@BookingActivity)
                    .getAvailableSlots(courtId, selectedDate)

                if (response.isSuccessful && response.body()?.success == true) {
                    val slots = response.body()!!.data ?: emptyList()
                    slotAdapter.submitSlots(slots)
                    binding.rvSlots.show()
                } else {
                    toast("Lapangan tidak beroperasi pada hari ini")
                    binding.cardSlots.hide()
                }
            } catch (e: Exception) {
                toast("Gagal memuat slot waktu")
                binding.cardSlots.hide()
            } finally {
                binding.progressSlots.hide()
            }
        }
    }

    private fun updateSummary() {
        if (selectedDate.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
            binding.cardSummary.hide()
            binding.btnConfirmBooking.isEnabled = false
            binding.btnConfirmBooking.alpha = 0.6f
            return
        }

        // Hitung durasi dan harga
        val startHour = selectedStartTime.split(":")[0].toInt()
        val endHour   = selectedEndTime.split(":")[0].toInt()
        val duration  = endHour - startHour

        // Ambil harga dari adapter slot yang dipilih
        val total = pricePerHour * duration

        binding.tvSummaryDate.text     = selectedDate.toDisplayDate()
        binding.tvSummaryTime.text     = "$selectedStartTime – $selectedEndTime"
        binding.tvSummaryDuration.text = "$duration jam"
        binding.tvSummaryPrice.text    = total.toRupiah()

        binding.cardSummary.show()
        binding.btnConfirmBooking.isEnabled = true
        binding.btnConfirmBooking.alpha = 1.0f
    }

    private fun confirmBooking() {
        if (selectedDate.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
            toast("Pilih tanggal dan slot waktu terlebih dahulu")
            return
        }

        binding.btnConfirmBooking.isEnabled = false
        binding.btnConfirmBooking.text = "Memproses..."

        lifecycleScope.launch {
            try {
                val request = BookingRequest(
                    courtId     = courtId,
                    bookingDate = selectedDate,
                    startTime   = "$selectedStartTime:00",  // API butuh HH:mm:ss
                    endTime     = "$selectedEndTime:00",    // API butuh HH:mm:ss
                    notes       = binding.etNotes.text?.toString()?.trim()
                )

                val response = RetrofitClient.getInstance(this@BookingActivity)
                    .createBooking(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val booking = response.body()!!.data!!
                    toast("Booking berhasil! Lanjutkan ke pembayaran.")

                    // Langsung ke PaymentActivity untuk pilih metode & upload bukti
                    val intent = Intent(this@BookingActivity, PaymentActivity::class.java)
                    intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.id)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    toast(response.body()?.message ?: "Booking gagal")
                    binding.btnConfirmBooking.isEnabled = true
                    binding.btnConfirmBooking.text = "Konfirmasi Booking"
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
                binding.btnConfirmBooking.isEnabled = true
                binding.btnConfirmBooking.text = "Konfirmasi Booking"
            }
        }
    }

    // Dipanggil dari HomeFragment/CourtDetailActivity dengan data harga
    fun setPricePerHour(price: Double) {
        pricePerHour = price
    }
}
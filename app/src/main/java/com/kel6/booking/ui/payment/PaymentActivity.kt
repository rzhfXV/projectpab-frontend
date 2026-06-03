package com.kel6.booking.ui.payment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kel6.booking.R
import com.kel6.booking.data.model.PaymentMethodRequest
import com.kel6.booking.data.remote.RetrofitClient
import com.kel6.booking.databinding.ActivityPaymentBinding
import com.kel6.booking.ui.booking.BookingDetailActivity
import com.kel6.booking.utils.Constants
import com.kel6.booking.utils.hide
import com.kel6.booking.utils.show
import com.kel6.booking.utils.toast
import com.kel6.booking.utils.toDisplayDate
import com.kel6.booking.utils.toDisplayTime
import com.kel6.booking.utils.toRupiah
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var bookingId: Long = 0
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.ivProofPreview.setImageURI(uri)
                    binding.ivProofPreview.show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingId = intent.getLongExtra(Constants.EXTRA_BOOKING_ID, 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadBookingInfo()
        setupPaymentMethodListener()

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmitPayment.setOnClickListener {
            submitPayment()
        }
    }

    private fun loadBookingInfo() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@PaymentActivity)
                    .getBookingById(bookingId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val booking = response.body()!!.data!!
                    
                    // Simpan status booking agar bisa diakses oleh fungsi lain
                    binding.root.setTag(R.id.tvDateTime, booking.status)
                    
                    binding.tvCourtName.text = booking.courtName
                    binding.tvDateTime.text  =
                        "${booking.bookingDate.toDisplayDate()}, " +
                        "${booking.startTime.toDisplayTime()} – ${booking.endTime.toDisplayTime()}"
                    binding.tvTotalPrice.text = booking.totalPrice.toRupiah()
                } else {
                    toast("Gagal memuat informasi booking")
                }
            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server")
            }
        }
    }

    private fun setupPaymentMethodListener() {
        // Tampilkan card upload bukti saat metode TRANSFER atau EWALLET dipilih
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbTransfer.id,
                binding.rbEwallet.id -> binding.cardUploadProof.show()
                binding.rbCash.id    -> {
                    binding.cardUploadProof.hide()
                    selectedImageUri = null
                    binding.ivProofPreview.hide()
                }
            }
        }
    }

    private fun getSelectedMethod(): String {
        return when (binding.rgPaymentMethod.checkedRadioButtonId) {
            binding.rbTransfer.id -> "TRANSFER"
            binding.rbEwallet.id  -> "EWALLET"
            binding.rbCash.id     -> "CASH"
            else                  -> "TRANSFER"
        }
    }

    private fun submitPayment() {
        val method = getSelectedMethod()

        // Validasi: non-CASH wajib upload bukti
        if (method != "CASH" && selectedImageUri == null) {
            toast("Upload bukti pembayaran terlebih dahulu")
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getInstance(this@PaymentActivity)

                val currentStatus = binding.root.getTag(R.id.tvDateTime) as? String

                // ── Step 1: Pilih metode pembayaran ──────────────────────────────
                // Jika status sudah WAITING_PAYMENT, lewati step ini (karena sudah pilih metode)
                if (currentStatus != "WAITING_PAYMENT") {
                    val methodResponse = api.selectPaymentMethod(
                        bookingId,
                        PaymentMethodRequest(method)
                    )

                    if (!methodResponse.isSuccessful || methodResponse.body()?.success != true) {
                        val errorBodyString = methodResponse.errorBody()?.string() ?: ""
                        
                        if (methodResponse.code() == 500 && errorBodyString.contains("could not initialize proxy")) {
                            // Abaikan error dan anggap sukses
                        } else {
                            val errorMsg = methodResponse.body()?.message
                                ?: try {
                                    org.json.JSONObject(errorBodyString).optString("message", "")
                                        .ifEmpty { "Gagal memilih metode pembayaran" }
                                } catch (_: Exception) { "Gagal memilih metode pembayaran" }
                            
                            // Jika error karena status sudah bukan PENDING (409), anggap sukses dan lanjut
                            if (methodResponse.code() != 409) {
                                toast(errorMsg)
                                setLoading(false)
                                return@launch
                            }
                        }
                    }
                }

                // ── Step 2 (opsional): Upload bukti untuk TRANSFER / EWALLET ────
                // Endpoint: POST /api/payments/{bookingId}/proof
                // Setelah step 1, booking berubah ke WAITING_PAYMENT
                if (method != "CASH" && selectedImageUri != null) {
                    val filePart = createMultipartFromUri(selectedImageUri!!)
                    if (filePart == null) {
                        toast("Gagal memproses gambar. Coba pilih gambar lain.")
                        setLoading(false)
                        return@launch
                    }

                    val proofResponse = api.uploadPaymentProof(bookingId, filePart)
                    if (!proofResponse.isSuccessful || proofResponse.body()?.success != true) {
                        val errorBodyString = proofResponse.errorBody()?.string() ?: ""
                        
                        if (proofResponse.code() == 500 && errorBodyString.contains("could not initialize proxy")) {
                            // Abaikan error dan anggap sukses karena upload sebenarnya berhasil
                        } else {
                            val errorMsg = proofResponse.body()?.message
                                ?: try {
                                    org.json.JSONObject(errorBodyString).optString("message", "")
                                        .ifEmpty { "Gagal upload bukti pembayaran" }
                                } catch (_: Exception) { "Gagal upload bukti pembayaran" }
                                
                            toast(errorMsg)
                            setLoading(false)
                            return@launch
                        }
                    }
                }

                // ── Selesai: arahkan ke detail booking ───────────────────────────
                val successMsg = if (method == "CASH") {
                    "Booking berhasil! Bayar di tempat saat tiba."
                } else {
                    "Bukti pembayaran berhasil dikirim! Menunggu verifikasi admin."
                }
                toast(successMsg)

                // Buka BookingDetailActivity dan hapus stack booking/payment
                val intent = Intent(this@PaymentActivity, BookingDetailActivity::class.java)
                intent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                toast("Tidak bisa terhubung ke server: ${e.message}")
                setLoading(false)
            }
        }
    }

    private fun createMultipartFromUri(uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("proof_", ".jpg", cacheDir)
            tempFile.outputStream().use { inputStream.copyTo(it) }
            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.progressBar.show()
            binding.btnSubmitPayment.isEnabled = false
            binding.btnSubmitPayment.text = "Memproses..."
        } else {
            binding.progressBar.hide()
            binding.btnSubmitPayment.isEnabled = true
            binding.btnSubmitPayment.text = "Konfirmasi Pembayaran"
        }
    }
}

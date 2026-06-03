package com.kel6.booking.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── View ──────────────────────────────────────────────────────
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// ── Toast ─────────────────────────────────────────────────────
fun Context.toast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Fragment.toast(msg: String) =
    requireContext().toast(msg)

// ── Snackbar ──────────────────────────────────────────────────
fun View.snackbar(msg: String) =
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()

// ── Navigation ────────────────────────────────────────────────
inline fun <reified T : Activity> Context.startActivity(
    clearStack: Boolean = false,
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply(block)
    if (clearStack) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

// ── Format rupiah ─────────────────────────────────────────────
fun Double.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace(",00", "")
}

// ── Format tanggal ────────────────────────────────────────────
fun String.toDisplayDate(): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        output.format(input.parse(this)!!)
    } catch (e: Exception) { this }
}

fun String.toDisplayTime(): String {
    // "08:00:00" → "08:00"
    return if (length >= 5) substring(0, 5) else this
}

// ── Status booking → warna & label ───────────────────────────
fun String.bookingStatusColor(): Int {
    return when (this) {
        "CONFIRMED" -> android.R.color.holo_green_dark
        "PENDING"   -> android.R.color.holo_orange_dark
        "WAITING_PAYMENT" -> android.R.color.holo_blue_dark
        "CANCELLED", "REJECTED" -> android.R.color.holo_red_dark
        "DONE"      -> android.R.color.darker_gray
        else        -> android.R.color.darker_gray
    }
}

fun String.bookingStatusLabel(): String {
    return when (this) {
        "PENDING"         -> "Pilih Metode Pembayaran"
        "WAITING_PAYMENT" -> "Menunggu Upload Bukti"
        "CONFIRMED"       -> "Dikonfirmasi"
        "REJECTED"        -> "Ditolak"
        "CANCELLED"       -> "Dibatalkan"
        "DONE"            -> "Selesai"
        else              -> this
    }
}
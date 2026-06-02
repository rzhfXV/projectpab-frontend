package com.kel6.booking.data.model

data class BookingResponse(
    val id: Long,
    val bookingDate: String,
    val startTime: String,
    val endTime: String,
    val durationHours: Double,
    val totalPrice: Double,
    val status: String,
    val notes: String?,
    val qrToken: String?,
    val createdAt: String,
    val courtId: Long,
    val courtName: String,
    val courtPhotoUrl: String?,
    val paymentMethod: String?,
    val paymentStatus: String?,
    val proofImageUrl: String?
)

data class BookingRequest(
    val courtId: Long,
    val bookingDate: String,   // format: "2025-07-01"
    val startTime: String,     // format: "08:00"
    val endTime: String,       // format: "10:00"
    val notes: String? = null
)
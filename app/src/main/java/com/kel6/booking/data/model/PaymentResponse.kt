package com.kel6.booking.data.model

data class PaymentResponse(
    val id: Long,
    val bookingId: Long,
    val method: String,
    val amount: Double,
    val proofImageUrl: String?,
    val status: String,
    val verifiedAt: String?,
    val verifiedBy: String?
)

data class PaymentMethodRequest(
    val method: String  // "TRANSFER", "EWALLET", "CASH"
)
package com.kel6.booking.data.model

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String
)

data class UnreadCountResponse(
    val count: Long
)
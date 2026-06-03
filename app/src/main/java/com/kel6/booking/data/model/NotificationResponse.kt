package com.kel6.booking.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    @SerializedName("isRead") val read: Boolean,
    val createdAt: String
)

data class UnreadCountResponse(
    val count: Long
)
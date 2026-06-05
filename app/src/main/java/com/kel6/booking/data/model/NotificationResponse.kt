package com.kel6.booking.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    // Backend uses Lombok @Data which serializes boolean isRead as "read" in JSON
    @SerializedName("read") val read: Boolean = false,
    val type: String? = null,
    val createdAt: String
)

data class UnreadCountResponse(
    val count: Long
)
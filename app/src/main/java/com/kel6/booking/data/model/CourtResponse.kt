package com.kel6.booking.data.model

import com.google.gson.annotations.SerializedName

data class CourtResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val pricePerHour: Double,
    val photoUrl: String?,
    @SerializedName("active") val isActive: Boolean,
    val schedules: List<ScheduleInfo>?
)

data class ScheduleInfo(
    val dayOfWeek: Int,
    val dayName: String,
    val openTime: String,
    val closeTime: String
)

data class SlotResponse(
    val startTime: String,
    val endTime: String,
    val available: Boolean
)
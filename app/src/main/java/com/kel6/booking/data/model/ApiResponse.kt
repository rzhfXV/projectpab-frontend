package com.kel6.booking.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
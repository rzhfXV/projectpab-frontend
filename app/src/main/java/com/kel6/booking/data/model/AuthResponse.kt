package com.kel6.booking.data.model

data class AuthResponse(
    val token: String,
    val tokenType: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
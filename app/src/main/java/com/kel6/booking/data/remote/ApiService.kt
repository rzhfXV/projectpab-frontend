package com.kel6.booking.data.remote

import com.kel6.booking.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ──────────────────────────────────────────────────────────
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/auth/me")
    suspend fun getMe(): Response<ApiResponse<UserInfo>>

    // ── COURTS ────────────────────────────────────────────────────────
    @GET("api/courts")
    suspend fun getCourts(): Response<ApiResponse<List<CourtResponse>>>

    @GET("api/courts/{id}")
    suspend fun getCourtById(@Path("id") id: Long): Response<ApiResponse<CourtResponse>>

    @GET("api/courts/{id}/slots")
    suspend fun getAvailableSlots(
        @Path("id") id: Long,
        @Query("date") date: String   // format: "2025-07-01"
    ): Response<ApiResponse<List<SlotResponse>>>

    // ── BOOKINGS ──────────────────────────────────────────────────────
    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<ApiResponse<BookingResponse>>

    @GET("api/bookings/my")
    suspend fun getMyBookings(): Response<ApiResponse<List<BookingResponse>>>

    @GET("api/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): Response<ApiResponse<BookingResponse>>

    @PATCH("api/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long): Response<ApiResponse<BookingResponse>>

    // ── PAYMENTS ──────────────────────────────────────────────────────
    @POST("api/payments/{bookingId}/method")
    suspend fun selectPaymentMethod(
        @Path("bookingId") bookingId: Long,
        @Body request: PaymentMethodRequest
    ): Response<ApiResponse<PaymentResponse>>

    @Multipart
    @POST("api/payments/{bookingId}/proof")
    suspend fun uploadPaymentProof(
        @Path("bookingId") bookingId: Long,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<PaymentResponse>>

    @GET("api/payments/{bookingId}")
    suspend fun getPayment(@Path("bookingId") bookingId: Long): Response<ApiResponse<PaymentResponse>>

    // ── NOTIFICATIONS ─────────────────────────────────────────────────
    @GET("api/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<NotificationResponse>>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>

    @PATCH("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long): Response<ApiResponse<Void>>

    @PATCH("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Void>>
}
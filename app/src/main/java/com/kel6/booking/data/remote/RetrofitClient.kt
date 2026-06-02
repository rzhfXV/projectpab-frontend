package com.kel6.booking.data.remote

import android.content.Context
import com.kel6.booking.R
//import com.kel6.booking.BuildConfig
import com.kel6.booking.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var instance: ApiService? = null

    fun getInstance(context: Context): ApiService {
        if (instance == null) {
            instance = buildRetrofit(context).create(ApiService::class.java)
        }
        return instance!!
    }

    private fun buildRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.base_url))  // ambil dari strings.xml
            .client(buildOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun buildOkHttpClient(context: Context): OkHttpClient {
        // Logging — tampilkan request/response di Logcat saat debug
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Auth Interceptor — otomatis sisipkan token JWT di setiap request
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking {
                UserPreferences(context).token.first()
            }
            val request = if (!token.isNullOrEmpty()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
package com.secverge.ai.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://ai.secverge.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }

    fun create(
        tokenProvider: AuthTokenProvider = NoOpAuthTokenProvider(),
        isDebug: Boolean = false
    ): SECVergeApi {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = tokenProvider.getToken()

                val requestBuilder = chain
                    .request()
                    .newBuilder()
                    .header("Accept", chain.request().header("Accept") ?: "application/json")

                if (!token.isNullOrBlank()) {
                    requestBuilder.header(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor { chain ->
                val started = System.nanoTime()
                val request = chain.request()
                try {
                    val response = chain.proceed(request)
                    if (com.secverge.ai.BuildConfig.DEBUG) {
                        android.util.Log.i("SECVergeHttp", "${request.method} ${request.url.encodedPath} HTTP ${response.code} elapsedMs=${(System.nanoTime() - started) / 1_000_000}")
                    }
                    response
                } catch (failure: java.io.IOException) {
                    if (com.secverge.ai.BuildConfig.DEBUG) {
                        android.util.Log.i("SECVergeHttp", "${request.method} ${request.url.encodedPath} failed=${failure.javaClass.simpleName} elapsedMs=${(System.nanoTime() - started) / 1_000_000}")
                    }
                    throw failure
                }
            }
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()

        return retrofit.create(SECVergeApi::class.java)
    }
}

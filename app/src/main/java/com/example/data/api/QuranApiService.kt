package com.example.data.api

import com.example.config.AppConfig
import com.example.data.model.BaseApiResponse
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.data.model.TafsirDetail
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface QuranApiService {
    @GET("surat")
    suspend fun getAllSurah(): BaseApiResponse<List<SurahItem>>

    @GET("surat/{nomor}")
    suspend fun getSurahDetail(@Path("nomor") nomor: Int): BaseApiResponse<SurahDetail>

    @GET("tafsir/{nomor}")
    suspend fun getTafsirDetail(@Path("nomor") nomor: Int): BaseApiResponse<TafsirDetail>
}

object QuranNetworkClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: QuranApiService = retrofit.create(QuranApiService::class.java)
}

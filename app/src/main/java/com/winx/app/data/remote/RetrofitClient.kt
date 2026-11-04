package com.winx.app.data.remote

import com.winx.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val BASE_URL = BuildConfig.API_BASE_URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: WinxApiService by lazy {
        retrofit.create(WinxApiService::class.java)
    }

    val mediaApi: MediaApiService by lazy {
        retrofit.create(MediaApiService::class.java)
    }
}

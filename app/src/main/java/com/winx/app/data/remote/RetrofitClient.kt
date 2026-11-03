package com.winx.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:5001/"

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

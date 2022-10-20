package com.colledk.obj3d.parser

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    fun getClient(): ApiService {
        return Retrofit.Builder().baseUrl("https://localhost:8000/").addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }
}
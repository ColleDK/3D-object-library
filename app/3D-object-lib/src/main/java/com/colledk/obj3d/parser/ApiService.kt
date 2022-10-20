package com.colledk.obj3d.parser

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @Streaming
    @GET
    suspend fun getFromUrl(@Url url: String): Response<ResponseBody>
}
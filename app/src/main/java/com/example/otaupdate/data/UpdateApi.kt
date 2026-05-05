package com.example.otaupdate.data

import retrofit2.http.GET

interface UpdateApi {
    @GET("user/pos")
    suspend fun getLatestVersion(): UpdateResponse
}
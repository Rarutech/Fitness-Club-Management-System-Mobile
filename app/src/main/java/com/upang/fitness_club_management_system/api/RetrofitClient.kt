package com.upang.fitness_club_management_system.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://glider-above-hopefully.ngrok-free.app/PumpingIronGym/"

    val gson = GsonBuilder()
        .setLenient()
        .create()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getBaseImageUrl(): String {
        return BASE_URL
    }
}

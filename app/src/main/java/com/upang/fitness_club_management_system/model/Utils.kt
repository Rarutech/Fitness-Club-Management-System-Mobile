package com.upang.fitness_club_management_system.model

import android.content.Context
import android.content.Intent
import android.util.Log
import com.upang.fitness_club_management_system.LoginPage
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import retrofit2.Call
import retrofit2.Response

object Utils {
    fun checkAuthentication(context: Context) {
        Log.d("Authentication","Auth check started")
        val preferenceManager = PreferenceManager(context)
        val email = preferenceManager.getEmail()
        val token = preferenceManager.getToken()
        preferenceManager.getToken()

        if (email.isNullOrEmpty() || token.isNullOrEmpty()){
            Log.d("Authentication", "token or email is empty")
            Log.d("Authentication","token: ${token} email: ${email}")
            val intent = Intent(context, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            return
        }

        val authRequest = AuthRequest(email,token)
        val api = RetrofitClient.instance.create(Api::class.java)

        api.Authenticate(authRequest).enqueue(object : retrofit2.Callback<AuthResponse>{
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful){
                    val result = response.body()
                    if (result?.message != "Authentication successful") {
                        val intent = Intent(context, LoginPage::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    } else {
                       Log.d("Authentication", "Successful")
                    }

                } else {
                    Log.e("Authentication", "Authentication failed with response: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("Authentication", "Network error: ${t.message}")
            }

        })
    }
}
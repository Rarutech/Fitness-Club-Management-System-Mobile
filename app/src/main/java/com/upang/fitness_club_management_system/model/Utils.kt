package com.upang.fitness_club_management_system.model

import android.content.Context
import android.content.Intent
import android.util.Log
import com.upang.fitness_club_management_system.LoginPage
import com.upang.fitness_club_management_system.MembershipFee
import com.upang.fitness_club_management_system.Trainee_Home
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
                    val intent = Intent(context, LoginPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("Authentication", "Network error: ${t.message}")
            }

        })
    }

    fun membershipAuthentication(context: Context) {
        Log.d("MembershipAuth", "Membership authentication check started")
        val preferenceManager = PreferenceManager(context)
        val email = preferenceManager.getEmail()

        if (email.isNullOrEmpty()) {
            Log.d("MembershipAuth", "Email is empty, redirecting to login")
            val intent = Intent(context, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)

        api.authenticateMembership(email).enqueue(object : retrofit2.Callback<memberAuthResponse> {
            override fun onResponse(call: Call<memberAuthResponse>, response: Response<memberAuthResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("MembershipAuth", "Response received: $result")

                    if (result != null) {
                        Log.d("MembershipAuth", "Parsed is_active value: ${result.is_active}")
                        if (result.is_active == true) {
                            Log.d("MembershipAuth", "Membership active, proceeding")
                        } else {
                            Log.d("MembershipAuth", "Membership inactive, redirecting to MembershipFee activity")
                            val intent = Intent(context, MembershipFee::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    } else {
                        Log.e("MembershipAuth", "Response body is null")
                    }
                } else {
                    Log.e("MembershipAuth", "Failed to authenticate membership: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<memberAuthResponse>, t: Throwable) {
                Log.e("MembershipAuth", "Network error: ${t.message}")
            }
        })
    }

    fun membershipAuthenticationUpdate(context: Context) {
        Log.d("MembershipAuth", "Membership authentication check started")
        val preferenceManager = PreferenceManager(context)
        val email = preferenceManager.getEmail()

        if (email.isNullOrEmpty()) {
            Log.d("MembershipAuth", "Email is empty, redirecting to login")
            val intent = Intent(context, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)

        api.authenticateMembership(email).enqueue(object : retrofit2.Callback<memberAuthResponse> {
            override fun onResponse(call: Call<memberAuthResponse>, response: Response<memberAuthResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("MembershipAuth", "Response received: $result")

                    if (result != null) {
                        Log.d("MembershipAuth", "Parsed is_active value: ${result.is_active}")
                        if (result.is_active == true) {
                            Log.d("MembershipAuth", "Membership active, proceeding")
                            val intent = Intent(context, Trainee_Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        } else {
                            return
                        }
                    } else {
                        Log.e("MembershipAuth", "Response body is null")
                    }
                } else {
                    Log.e("MembershipAuth", "Failed to authenticate membership: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<memberAuthResponse>, t: Throwable) {
                Log.e("MembershipAuth", "Network error: ${t.message}")
            }
        })
    }
}
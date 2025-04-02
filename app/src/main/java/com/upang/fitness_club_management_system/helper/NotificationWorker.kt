package com.upang.fitness_club_management_system.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.fetchNotificationResponse
import retrofit2.Call
import retrofit2.Response

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        getNotifications(context) // Fetch notifications
        return Result.success()
    }

    fun getNotifications(context: Context) {
        val preferenceManager = PreferenceManager(context)
        val email = preferenceManager.getEmail()

        if (email == null) {
            Log.e("NotificationWorker", "No email found, cannot fetch notifications.")
            return
        }

        Log.d("NotificationWorker", "Fetching notifications for email: $email")

        val api = RetrofitClient.instance.create(Api::class.java)

        api.getNotification(email).enqueue(object : retrofit2.Callback<fetchNotificationResponse> {
            override fun onResponse(
                call: Call<fetchNotificationResponse>,
                response: Response<fetchNotificationResponse>
            ) {
                if (response.isSuccessful) {
                    val notificationResponse = response.body()
                    if (notificationResponse != null) {
                        Log.d("NotificationWorker", "Fetched ${notificationResponse.notifications.size} notifications.")

                        notificationResponse.notifications.forEach { notif ->
                            Log.d("NotificationWorker", "Displaying notification: ${notif.message}")
                            showNotification(context, notif.message)
                        }
                    } else {
                        Log.e("NotificationWorker", "Response body is null")
                    }
                } else {
                    Log.e("NotificationWorker", "API Response failed with code: ${response.code()} and message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<fetchNotificationResponse>, t: Throwable) {
                Log.e("NotificationWorker", "API call failed: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "notifications_channel"
        val notificationId = System.currentTimeMillis().toInt()

        Log.d("NotificationWorker", "Preparing to show notification: $message")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotificationWorker", "Notification permission not granted.")
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("NotificationWorker", "Creating notification channel...")

            val channel = NotificationChannel(
                channelId,
                "Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gym Notifications"
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        Log.d("NotificationWorker", "Building notification...")

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.pumping_iron_login_logo) // Ensure this icon exists
            .setContentTitle("New Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            Log.d("NotificationWorker", "Displaying notification...")
            notify(notificationId, notification)
        }

        Log.d("NotificationWorker", "Notification displayed successfully.")
    }
}

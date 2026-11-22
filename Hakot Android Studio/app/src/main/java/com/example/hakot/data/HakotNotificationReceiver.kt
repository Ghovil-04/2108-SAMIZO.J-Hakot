package com.example.hakot.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HakotNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Hakot Schedule Reminder"
        val message = intent.getStringExtra("message") ?: "Your waste collection schedule is coming up."
        HakotRepository.showSystemNotification(context, title, message)
    }
}

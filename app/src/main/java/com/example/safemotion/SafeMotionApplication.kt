package com.example.safemotion

import android.app.Application
import com.example.safemotion.notifications.FirestoreNotificationObserver

class SafeMotionApplication : Application() {
    private lateinit var notifications: FirestoreNotificationObserver

    override fun onCreate() {
        super.onCreate()
        notifications = FirestoreNotificationObserver(this)
        notifications.start()
    }
}

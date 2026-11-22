package com.example.safemotion.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.safemotion.MainActivity
import com.example.safemotion.R
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.repository.FirestoreGuardianRepository
import com.example.safemotion.data.repository.FirestoreGuardianTrackingRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

class FirestoreNotificationObserver(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val manager = context.getSystemService(NotificationManager::class.java)
    private var currentUid: String? = null
    private var alertsJob: Job? = null
    private var invitationsJob: Job? = null

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val uid = auth.currentUser?.uid
        if (uid != currentUid) {
            alertsJob?.cancel()
            invitationsJob?.cancel()
            currentUid = uid
            if (uid != null) observe(uid)
        }
    }

    fun start() {
        createChannels()
        Firebase.auth.addAuthStateListener(authListener)
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(
                ALERT_CHANNEL, context.getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                INVITATION_CHANNEL, context.getString(R.string.notification_channel_invitations),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun observe(uid: String) {
        val db = Firebase.firestore
        val startedAtMillis = System.currentTimeMillis()
        val alertTracker = NewDocumentTracker(startedAtMillis)
        alertsJob = scope.launch {
            db.collection(FirestoreGuardianTrackingRepository.ALERTS)
                .whereArrayContains("guardianUids", uid)
                .snapshots()
                .retryWhen { cause, attempt ->
                    if (cause is CancellationException) return@retryWhen false
                    Log.w(TAG, context.getString(R.string.notification_alert_listener_error), cause)
                    delay(((attempt + 1).coerceAtMost(5)) * 1_000L)
                    true
                }
                .collect { snapshot ->
                    val documents = snapshot.documents.associateBy { it.id }
                    val timestamps = documents.mapValues { it.value.getTimestamp("createdAt")?.toDate()?.time }
                    for (id in alertTracker.newIds(timestamps)) {
                        val alert = documents.getValue(id)
                        val name = alert.getString("runnerName").orEmpty()
                            .ifBlank { context.getString(R.string.notification_unknown_runner) }
                        val kind = when (alert.getString("type")) {
                            IncidentType.FALL.name -> context.getString(R.string.notification_incident_fall)
                            IncidentType.INACTIVITY.name -> context.getString(R.string.notification_incident_inactivity)
                            else -> context.getString(R.string.notification_incident_other)
                        }
                        notify(
                            ALERT_CHANNEL, id,
                            context.getString(R.string.notification_alert_title, name),
                            context.getString(R.string.notification_alert_body, name, kind)
                        )
                    }
                }
        }
        val invitationTracker = NewDocumentTracker(startedAtMillis)
        invitationsJob = scope.launch {
            db.collection(FirestoreGuardianRepository.INVITATIONS)
                .whereEqualTo("toUid", uid)
                .whereEqualTo("status", InvitationStatus.PENDING.name)
                .snapshots()
                .retryWhen { cause, attempt ->
                    if (cause is CancellationException) return@retryWhen false
                    Log.w(TAG, context.getString(R.string.notification_invitation_listener_error), cause)
                    delay(((attempt + 1).coerceAtMost(5)) * 1_000L)
                    true
                }
                .collect { snapshot ->
                    val documents = snapshot.documents.associateBy { it.id }
                    val timestamps = documents.mapValues { it.value.getTimestamp("createdAt")?.toDate()?.time }
                    for (id in invitationTracker.newIds(timestamps)) {
                        val name = documents.getValue(id).getString("fromName").orEmpty()
                            .ifBlank { context.getString(R.string.notification_unknown_runner) }
                        notify(
                            INVITATION_CHANNEL, id,
                            context.getString(R.string.notification_invitation_title),
                            context.getString(R.string.notification_invitation_body, name)
                        )
                    }
                }
        }
    }

    private fun notify(channelId: String, documentId: String, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (channelId == ALERT_CHANNEL) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify("$channelId/$documentId".hashCode(), notification)
    }

    private companion object {
        const val TAG = "SafeMotionNotifications"
        const val ALERT_CHANNEL = "guardian_alerts"
        const val INVITATION_CHANNEL = "guardian_invitations"
    }
}

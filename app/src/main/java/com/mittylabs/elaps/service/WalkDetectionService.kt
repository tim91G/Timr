package com.mittylabs.elaps.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.*
import com.mittylabs.elaps.extensions.toasty
import com.mittylabs.elaps.model.TimerState
import com.mittylabs.elaps.notification.Notifications
import com.mittylabs.elaps.notification.NotificationsImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WalkDetectionService : Service() {

    @Inject
    lateinit var notifications: Notifications

    private lateinit var pendingIntent: PendingIntent
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private val request = ActivityTransitionRequest(
        listOf<ActivityTransition>(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
    )

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(
            NotificationsImpl.NOTIFICATION_ID,
            notifications.getOrCreateNotification(0, TimerState.Terminated)
        )

        activityRecognitionClient = ActivityRecognitionClient(this)

        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(TimerService.TRANSITIONS_RECEIVER_ACTION),
            PendingIntent.FLAG_IMMUTABLE
        )

        requestActivityUpdates()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        removeActivityUpdates()
    }

    private fun requestActivityUpdates() {
        // todo this consumes more battery
//        activityRecognitionClient.requestActivityUpdates(
//            DETECTION_INTERVAL_IN_MILLISECONDS,
//            pendingIntent
//        )

        activityRecognitionClient.requestActivityTransitionUpdates(
            request,
            pendingIntent
        ).addOnSuccessListener {
            toasty("Successfully requested activity updates")
        }.addOnFailureListener {
            toasty("Requesting activity updates failed to start")
        }
    }

    private fun removeActivityUpdates() {
        activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent)
            .addOnSuccessListener {
                toasty("Removed activity updates successfully!")
            }.addOnFailureListener {
                toasty("Failed to remove activity updates!")
            }
    }

    companion object {
        private const val DETECTION_INTERVAL_IN_MILLISECONDS: Long = 1000
    }
}
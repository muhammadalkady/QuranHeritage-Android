package com.kady.muhammad.quran.heritage.domain.player

import android.app.*
import android.app.Notification.VISIBILITY_PUBLIC
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity

object PlayerNotification {

    const val NOTIFICATION_ID = 1
    private const val NOTIFICATION_PLAYER_NOTIFICATION_CHANNEL_ID = "1"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context) {
        with(context) {
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val name: String = getString(R.string.user_notification_channel_name)
            val description: String = context.getString(R.string.user_notification_channel_desc)
            val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_PLAYER_NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = description
            channel.setShowBadge(false)
            channel.setSound(null, null)
            channel.lockscreenVisibility = VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify(
        app: Application,
        mediaSession: MediaSessionCompat,
        paused: Boolean
    ): Notification {
        /*
        * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(app)
        /*
        * */
        val contentIntent: PendingIntent = PendingIntent.getActivity(app, 0, Intent(app, MainActivity::class.java), 0)
        /*
        * */
        val builder = NotificationCompat.Builder(app, NOTIFICATION_PLAYER_NOTIFICATION_CHANNEL_ID)
        /*
        * */
        val contentText: String = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        val contentTitle: String = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
        /*
        * */
        val playPauseAction: NotificationCompat.Action = if (paused) {
            NotificationCompat.Action(
                R.drawable.ic_play_arrow,
                app.getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(app, PlaybackStateCompat.ACTION_PLAY)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                app.getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(app, PlaybackStateCompat.ACTION_PAUSE)
            )
        }
        val nextAction: NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_skip_next,
                app.getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(app, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            )
        val previousAction: NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_skip_previous,
                app.getString(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(app, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            )
        /*
        * */
        val style: androidx.media.app.NotificationCompat.MediaStyle =
            androidx.media.app.NotificationCompat
                .MediaStyle()
//                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(false)
                .setShowActionsInCompactView(1)
        /*
        * */
        builder.setStyle(style)
        builder.color = ContextCompat.getColor(app, R.color.colorPrimary)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setOnlyAlertOnce(true)
        /*
        * */
        builder.setLargeIcon(BitmapFactory.decodeResource(app.resources, R.drawable.notification_large_icon))
        builder.setSmallIcon(R.drawable.ic_notification)
        /*
        * */
        builder.setContentIntent(contentIntent)
        /*
        * */
        builder.setContentTitle(contentTitle)
        builder.setContentText(contentText)
        /*
        * */
        builder.addAction(nextAction)
        builder.addAction(playPauseAction)
        builder.addAction(previousAction)
        /*
        * */
        val notification: Notification = builder.build()
        val notificationManager: NotificationManager = app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }
}
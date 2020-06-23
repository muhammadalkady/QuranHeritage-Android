package com.kady.muhammad.quran.heritage.domain.player

import android.app.Notification
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.presentation.ext.textToBitmap
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
        app: PlayerService,
        mediaSession: MediaSessionCompat,
        paused: Boolean
    ): Notification =
        with(app) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(this)
            val bigNotificationLayout = RemoteViews(packageName, R.layout.player_notification_big)
            val smallNotificationLayout =
                RemoteViews(packageName, R.layout.player_notification_small)
            val customNotificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, NOTIFICATION_PLAYER_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this, 0,
                            Intent(this, MainActivity::class.java), 0
                        )
                    )
                    .setCustomBigContentView(bigNotificationLayout)
                    .setCustomContentView(smallNotificationLayout)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //
            val accentColor: Int = ContextCompat.getColor(applicationContext, R.color.colorAccent)
            //
            val playPauseImageRes: Int = if (paused) R.drawable.ic_play_arrow else R.drawable.ic_pause
            bigNotificationLayout.setImageViewResource(R.id.playPause, playPauseImageRes)
            smallNotificationLayout.setImageViewResource(R.id.playPause, playPauseImageRes)
            //
            val pendingIntent: PendingIntent = if (paused) MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
            else MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)
            bigNotificationLayout.setOnClickPendingIntent(R.id.playPause, pendingIntent)
            smallNotificationLayout.setOnClickPendingIntent(R.id.playPause, pendingIntent)
            //
            val title: String = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            val titleBitmap: Bitmap = textToBitmap(title, 48F, accentColor, true)
            bigNotificationLayout.setImageViewBitmap(R.id.trackName, titleBitmap)
            smallNotificationLayout.setImageViewBitmap(R.id.trackName, titleBitmap)
            //
            val displayTitle: String = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            val displayTitleBitmap: Bitmap = textToBitmap(displayTitle, 48F, Color.WHITE, true)
            bigNotificationLayout.setImageViewBitmap(R.id.playListName, displayTitleBitmap)
            smallNotificationLayout.setImageViewBitmap(R.id.playListName, displayTitleBitmap)
            //
            val customNotification: Notification = customNotificationBuilder.build()
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, customNotification)
            return@with customNotification
        }
}
package com.kady.muhammad.quran.heritage.domain.player

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const

class PlayerService : MediaBrowserServiceCompat() {

    private val tag = "Player-Service"

    private lateinit var player: Player
    private lateinit var mutableMediaSession: MediaSessionCompat

    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onCustomAction(action: String, extras: Bundle) {
            when (action) {
                Const.PLAYER_ACTION_SKIP_TO_MEDIA_ID -> player.seekToChild(
                    extras.getString(
                        Const.MEDIA_ID
                    )!!
                )
            }
        }

        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToNext() = player.next()
        override fun onSkipToPrevious() = player.previous()
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onSetRepeatMode(repeatMode: Int) = player.setRepeatMode(repeatMode)
        override fun onSetShuffleMode(shuffleMode: Int) = player.setShuffleMode(shuffleMode)
        override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) =
            player.setChildMediaId(mediaId).also { player.prepare() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return Service.START_NOT_STICKY
    }

    private val mediaSession by lazy { mutableMediaSession }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot =
        BrowserRoot("Root", null)

    override fun onCreate() {
        super.onCreate()
        Logger.logI(tag, "player service was created")
        initPlayer()
        initMediaSession()
        player.setMediaSession(mediaSession)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
        Logger.logI(tag, "player service was destroyed")
    }

    private fun initPlayer() {
        player = Player(this)
        player.init()
    }

    private fun cleanup() {
        Logger.logI(tag, "cleaning up ...")
        player.stop()
        player.release()
        mediaSession.release()
    }

    private fun initMediaSession(): MediaSessionCompat {
        Logger.logI(tag, "Initializing mainMedia session ...")
        val mediaSession = MediaSessionCompat(this, "Muhammad-Qadi")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mediaSession.setCallback(mediaSessionCallback)
        sessionToken = mediaSession.sessionToken
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        this.mutableMediaSession = mediaSession
        return mediaSession
    }

}
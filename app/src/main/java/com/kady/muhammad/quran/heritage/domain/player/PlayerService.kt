package com.kady.muhammad.quran.heritage.domain.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const

class PlayerService : MediaBrowserServiceCompat() {

    private val tag = "Player-Service"

    private lateinit var player: Player
    private lateinit var mutableMediaSession: MediaSessionCompat
    private val mediaSession by lazy { mutableMediaSession }
    private val playbackStateCompat = PlaybackStateCompat.Builder()
        .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
        .build()
    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {

        override fun onCustomAction(action: String, extras: Bundle) {
            Logger.logI(tag, "on custom action")
            when (action) {
                Const.PLAYER_ACTION_SKIP_TO_MEDIA_ID -> player.seekToChild(extras.getString(Const.MEDIA_ID)!!)
            }
        }

        override fun onPlay() {
            Logger.logI(tag, "on play")
            player.play()
        }

        override fun onPause() {
            Logger.logI(tag, "on pause")
            player.pause()
        }

        override fun onSkipToNext() {
            Logger.logI(tag, "on skip to next")
            player.next()
        }

        override fun onSkipToPrevious() {
            Logger.logI(tag, "on skip to previous")
            player.previous()
        }

        override fun onSeekTo(pos: Long) {
            Logger.logI(tag, "on seek to")
            player.seekTo(pos)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            Logger.logI(tag, "on set repeat mode")
            player.setRepeatMode(repeatMode)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            Logger.logI(tag, "on set shuffle mode")
            player.setShuffleMode(shuffleMode)
        }

        override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
            Logger.logI(tag, "on prepare from media id")
            player.setChildMediaId(mediaId).also { player.prepare() }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Logger.logI(tag, "on play from media id")
        }

        override fun onPrepare() {
            Logger.logI(tag, "on prepare")
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            Logger.logI(tag, "on play from search")
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            Logger.logI(tag, "on play from uri")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.logI(tag, "on start command ${intent?.action}")
        val keyEvent: KeyEvent? = MediaButtonReceiver.handleIntent(mediaSession, intent)
        Logger.logI(tag, "key event $keyEvent")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Logger.logI(tag, "on load children")
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

    private fun initMediaSession() {
        Logger.logI(tag, "Initializing main media session ...")
        val mediaSession = MediaSessionCompat(this, "Muhammad-Alkady")
        mediaSession.setCallback(mediaSessionCallback)
        sessionToken = mediaSession.sessionToken
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setPlaybackState(playbackStateCompat)
        mediaSession.isActive = true
        this.mutableMediaSession = mediaSession
    }

}
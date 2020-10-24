package com.kady.muhammad.quran.heritage.domain.player

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.kady.muhammad.quran.heritage.App
import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMediaId
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject
import com.kady.muhammad.quran.heritage.domain.player.Player as QuranPlayer

object Player : Runnable, AudioManager.OnAudioFocusChangeListener, KoinComponent {

    operator fun invoke(playerService: PlayerService): QuranPlayer {
        this.playerService = playerService
        return this
    }

    private const val tag = "Player"
    private const val elapsedTimeRefreshInterval = 1000L
    private const val userAgent = "Muhammad-Alkady"

    private val app: Application by lazy { playerService.applicationContext as App }
    private val playerHandlerThread = HandlerThread("player_handler_thread")
    private val playerHandler: Handler by lazy { Handler(playerHandlerThread.looper) }
    private val elapsedTimeHandler = Handler(Looper.myLooper()!!)
    private val audioFocusHandler: Handler by lazy { Handler(playerHandlerThread.looper) }
    private val playbackStateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
    private val noisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pause()
        }
    }
    private val api: API by inject()
    private val repo: MediaRepo by inject()

    private var isInit = false
    private var playOnFocus: Boolean = false
    private var isNoisyReceiverRegistered: Boolean = false
    private var childrenCount: Int = 0

    private lateinit var playerService: PlayerService
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var cache: SimpleCache
    private lateinit var dataSourceFactory: DefaultHttpDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSourceFactory
    private lateinit var defaultTrackSelector: DefaultTrackSelector
    private lateinit var defaultLoadControl: DefaultLoadControl
    private lateinit var defaultRendererFactory: RenderersFactory

    private lateinit var childMediaId: ChildMediaId

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var audioFocusRequest: AudioFocusRequest

    private val metadataBuilder: MediaMetadataCompat.Builder by lazy { MediaMetadataCompat.Builder() }
    private val wifiLock: WifiManager.WifiLock by lazy {
        (app.getSystemService(WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Muhammad-Alkady")
    }

    private val playerBundle = Bundle()

    private fun initComponents() {
        Logger.logI(tag, "initializing player components")
        cache = SimpleCache(
            playerService.cacheDir, LeastRecentlyUsedCacheEvictor(Long.MAX_VALUE),
            ExoDatabaseProvider(app)
        )
        dataSourceFactory = DefaultHttpDataSourceFactory(
            userAgent, 0,
            0, true
        )
        cacheDataSourceFactory = CacheDataSourceFactory(cache, dataSourceFactory)
        defaultTrackSelector = DefaultTrackSelector(app)
        defaultLoadControl = DefaultLoadControl()
        defaultRendererFactory = DefaultRenderersFactory(playerService)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAudioFocusRequest() {
        Logger.logI(tag, "initializing audio focus request")
        val audioAttr = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        audioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(audioAttr)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this@Player, audioFocusHandler).build()
    }

    private fun onAudioFocusLossTransientCanDuck() {
        playerHandler.post { simpleExoPlayer.volume = .3F }
    }

    private fun onAudioFocusLossTransient() {
        playOnFocus = true;pause()
    }

    private fun onAudioFocusLoss() {
        playOnFocus = false;pause()
    }

    private fun onAudioFocusGain() {
        playerHandler.post {
            if (isPlaying()) simpleExoPlayer.volume = 1F
            if (playOnFocus && !isPlaying()) play();playOnFocus = false
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus(): Boolean {
        Logger.logI(tag, "requesting audio focus")
        val audioManager = playerService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) audioManager.requestAudioFocus(
                audioFocusRequest
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            else audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Logger.logI(tag, "requesting audio focus was ${if (result) "granted" else "denied"} ")
        return result
    }

    @Suppress("DEPRECATION")
    private fun abandonAudioFocus() {
        Logger.logI(tag, "abandon audio focus")
        val audioManager = playerService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) audioManager.abandonAudioFocusRequest(
            audioFocusRequest
        )
        else audioManager.abandonAudioFocus(this)
    }

    private fun registerNoisyReceiver() {
        Logger.logI(tag, "register noisy receiver")
        val filter = IntentFilter()
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        playerService.registerReceiver(noisyReceiver, filter)
        isNoisyReceiverRegistered = true
    }

    private fun unregisterNoisyReceiver() {
        Logger.logI(tag, "unregister noisy receiver")
        if (isNoisyReceiverRegistered) playerService.unregisterReceiver(noisyReceiver)
        isNoisyReceiverRegistered = false
    }

    private fun isPlaying(): Boolean {
        return simpleExoPlayer.playWhenReady
    }

    private fun setMetadata(childMediaId: ChildMediaId) {
        mediaSession.setMetadata(buildMetadata(childMediaId))
    }

    private suspend fun allChildren(childMediaId: ChildMediaId): List<ChildMedia> {
        return repo.otherChildren(true, childMediaId)
    }

    private suspend fun currentChildMedia(childMediaId: ChildMediaId): Media {
        return allChildren(childMediaId)[simpleExoPlayer.currentWindowIndex]
    }

    private suspend fun parentMedia(childMediaId: ChildMediaId): Media {
        return repo.parentMediaForChildId(true, childMediaId)
    }

    private fun buildMetadata(childMediaId: ChildMediaId): MediaMetadataCompat {
        return runBlocking {
            val currentChild = currentChildMedia(childMediaId)
            val parent = parentMedia(childMediaId)
            metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, currentChild.id)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, parent.title)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentChild.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, simpleExoPlayer.duration)
            return@runBlocking metadataBuilder.build()
        }
    }

    private fun internalPrepare(allChildren: List<ChildMedia>) {
        childrenCount = allChildren.size
        val mediaSources: Array<ProgressiveMediaSource> = allChildren
            .map {
                ProgressiveMediaSource
                    .Factory(cacheDataSourceFactory)
                    .setLoadErrorHandlingPolicy(CustomLoadErrorLoadPolicy())
                    .createMediaSource(Uri.parse(api.streamUrl(it.parentId, it.id)))
            }.toTypedArray()
        val contactingMediaSource = ConcatenatingMediaSource(*mediaSources)
        simpleExoPlayer.prepare(contactingMediaSource, true, true)
    }

    private fun setPlaybackState(inPlaybackState: Int) {
        val actions = when (inPlaybackState) {
            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            else -> PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        val playbackState = playbackStateBuilder.setState(
            inPlaybackState,
            simpleExoPlayer.currentPosition,
            simpleExoPlayer.playbackParameters.speed
        ).setActions(actions).build()
        mediaSession.setPlaybackState(playbackState)
    }

    private fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> onBuffering()
            Player.STATE_ENDED -> onEnded()
            Player.STATE_IDLE -> onIdle()
            Player.STATE_READY -> onReady(playWhenReady)
        }
    }

    private fun onReady(playWhenReady: Boolean) {
        if (playWhenReady) {
            onPlay()
        } else {
            onPause()
        }
    }

    private fun onPause() {
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        elapsedTimeHandler.removeCallbacks(this)
        unregisterNoisyReceiver()
        playerService.stopForeground(false)
        PlayerNotification.notify(app, mediaSession, true)
    }

    private fun onPlay() {
        setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        setMetadata(childMediaId)
        elapsedTimeHandler.post(this)
        registerNoisyReceiver()
        playerService.startForeground(
            PlayerNotification.NOTIFICATION_ID,
            PlayerNotification.notify(app, mediaSession, false)
        )
    }

    private fun onIdle() {
        setPlaybackState(PlaybackStateCompat.STATE_NONE)
    }

    private fun onEnded() {
        abandonAudioFocus()
        playerService.stopForeground(true)
        setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
    }

    private fun onBuffering() {
        setMetadata(childMediaId)
        setPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
        playerService.startForeground(
            PlayerNotification.NOTIFICATION_ID,
            PlayerNotification.notify(app, mediaSession, false)
        )
    }

    private fun onPositionDiscontinuity() {
        setMetadata(childMediaId)
        playerService.startForeground(
            PlayerNotification.NOTIFICATION_ID,
            PlayerNotification.notify(app, mediaSession, false)
        )
    }

    private suspend fun ensureChildrenCount(childMediaId: ChildMediaId) {
        if (allChildren(childMediaId).size.apply {
                Logger.logI(tag, "ensure children count $this | $childrenCount")
            } != childrenCount)
            internalPrepare(allChildren(childMediaId)).also { seekToChild(childMediaId) }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> onAudioFocusGain()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onAudioFocusLossTransientCanDuck()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onAudioFocusLossTransient()
            AudioManager.AUDIOFOCUS_LOSS -> onAudioFocusLoss()
        }
    }

    override fun run() {
        playerHandler.post {
            elapsedTimeHandler.postDelayed(this, elapsedTimeRefreshInterval)
            playerBundle.putLong(Const.PLAYER_ELAPSED_TIME, simpleExoPlayer.currentPosition)
            mediaSession.sendSessionEvent(Const.PLAYER_ELAPSED_TIME_EVENT, playerBundle)
        }
    }

    fun seekToChild(childMediaId: ChildMediaId) {
        playerHandler.post {
            runBlocking {
                ensureChildrenCount(childMediaId)
                simpleExoPlayer.seekTo(
                    allChildren(childMediaId).indexOfFirst { it.id == childMediaId },
                    0
                )
            }
        }
    }

    fun play() {
        playerHandler.post {
            Logger.logI(tag, "play")
            runBlocking {
                val allCachedMedia = repo.allCachedMedia()
                if (allCachedMedia.isEmpty()) return@runBlocking
                if (!::childMediaId.isInitialized) {
                    runBlocking { childMediaId = allCachedMedia.first().id }
                }
                runBlocking { ensureChildrenCount(childMediaId) }
                wifiLock.acquire()
                if (requestAudioFocus()) simpleExoPlayer.playWhenReady = true
            }
        }
    }

    fun pause() {
        playerHandler.post {
            Logger.logI(tag, "pause")
            runBlocking { ensureChildrenCount(childMediaId) }
            if (wifiLock.isHeld) wifiLock.release()
            simpleExoPlayer.playWhenReady = false
            if (!playOnFocus) abandonAudioFocus()
            onPause()
        }
    }

    fun setChildMediaId(childMediaId: ChildMediaId) {
        this.childMediaId = childMediaId
    }

    fun init() {
        if (isInit) {
            Logger.logE(tag, "player is already initialized ... not initializing")
            return
        }
        Logger.logI(tag, "initializing")
        playerHandlerThread.start()
        playerHandler.post {
            initComponents()
            simpleExoPlayer = SimpleExoPlayer
                .Builder(app, defaultRendererFactory)
                .setTrackSelector(defaultTrackSelector)
                .setLoadControl(defaultLoadControl)
                .setBandwidthMeter(DefaultBandwidthMeter.Builder(app).build())
                .setLooper(playerHandler.looper)
                .build()
            simpleExoPlayer.addListener(Listener(this))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) initAudioFocusRequest()
            isInit = true
        }

    }

    fun setMediaSession(mediaSessionCompat: MediaSessionCompat) {
        this.mediaSession = mediaSessionCompat
    }

    fun seekTo(pos: Long) {
        playerHandler.post {
            if (!::childMediaId.isInitialized) return@post
            runBlocking { ensureChildrenCount(childMediaId) }
            simpleExoPlayer.seekTo(pos)
        }
    }

    fun prepare() {
        playerHandler.post {
            runBlocking {
                Logger.logI(tag, "prepare")
                internalPrepare(allChildren(childMediaId)).also { seekToChild(childMediaId) }
            }
        }
    }

    fun next() {
        playerHandler.post {
            Logger.logI(tag, "next")
            if (::childMediaId.isInitialized) {
                runBlocking { ensureChildrenCount(childMediaId) }
                with(simpleExoPlayer) {
                    val allChildren = runBlocking { repo.otherChildren(true, childMediaId) }
                    if (currentWindowIndex < allChildren.lastIndex) seekTo(
                        simpleExoPlayer.currentWindowIndex + 1,
                        0
                    )
                    else seekTo(0, 0)
                    if (!playWhenReady) play()
                }
            }
        }
    }

    fun previous() {
        playerHandler.post {
            Logger.logI(tag, "previous")
            if (::childMediaId.isInitialized) {
                runBlocking { ensureChildrenCount(childMediaId) }
                with(simpleExoPlayer) {
                    val allChildren = runBlocking { repo.otherChildren(true, childMediaId) }
                    if (currentWindowIndex == 0) seekTo(allChildren.lastIndex, 0)
                    else seekTo(currentWindowIndex - 1, 0)
                    if (!playWhenReady) play()
                }
            }
        }
    }

    fun setRepeatMode(repeatMode: Int) {
        playerHandler.post {
            Logger.logI(tag, "set repeat mode")
            simpleExoPlayer.repeatMode =
                if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            mediaSession.setRepeatMode(repeatMode)
        }
    }

    fun setShuffleMode(shuffleMode: Int) {
        playerHandler.post {
            Logger.logI(tag, "set shuffle mode")
            simpleExoPlayer.shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
            mediaSession.setShuffleMode(shuffleMode)
        }
    }

    fun stop() {
        playerHandler.post {
            Logger.logI(tag, "stop")
            simpleExoPlayer.stop()
        }
    }

    fun release() {
        playerHandler.post {
            Logger.logI(tag, "release")
            simpleExoPlayer.release()
            cache.release()
        }
    }

    class CustomLoadErrorLoadPolicy : DefaultLoadErrorHandlingPolicy() {
        override fun getMinimumLoadableRetryCount(dataType: Int): Int {
            return Int.MAX_VALUE
        }
    }

    object Listener : Player.EventListener {

        private lateinit var player: QuranPlayer

        operator fun invoke(player: QuranPlayer): Listener {
            this.player = player
            return this
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
        override fun onSeekProcessed() {}
        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
        }

        override fun onPositionDiscontinuity(reason: Int) = player.onPositionDiscontinuity()
        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) =
            player.onPlayerStateChanged(playWhenReady, playbackState)
    }

}
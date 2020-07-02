package com.kady.muhammad.quran.heritage.presentation.player

import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentPlayerBinding
import com.kady.muhammad.quran.heritage.domain.ext.millisToPlayerDuration
import com.kady.muhammad.quran.heritage.presentation.ext.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(), PanelSlideListener {

    private val vm: PlayerViewModel by lazy { ViewModelProvider(this).get(PlayerViewModel::class.java) }
    private lateinit var binding: FragmentPlayerBinding
    private var isUserSeeking: Boolean = false
    private val upLp = LinearLayout.LayoutParams(0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.connectMediaBrowser()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        setBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerPlayerState();observerPlayerMetadata();observerElapsedTime();observerRepeatOneMode();observerShuffleMode()
        setupUp()
        setSeekBarChangeListener()
        syncWithPanelLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.disconnectMediaBrowser()
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        view ?: return
        up.layoutParams = upLp.apply {
            width = 0
            height = LinearLayout.LayoutParams.MATCH_PARENT
            weight = 1 - slideOffset
        }
        up.translationX = slideOffset * up.width
        metaContainer.alpha = slideOffset
    }

    private fun setupUp() {
        upIcon.upAnimation()
    }

    private fun syncWithPanelLayout() {
        (activity as? PanelLayout)?.let {
            val panel: SlidingUpPanelLayout = it.getPanel()
            onPanelSlide(panel, if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F)
        }
    }

    private fun setBindingVariables() {
        binding.fm = this
        binding.vm = vm
    }

    private fun observerPlayerState() {
        vm.playerState.observe(viewLifecycleOwner, Observer {
            when (it) {
                PlaybackStateCompat.STATE_BUFFERING -> onBuffering()
                PlaybackStateCompat.STATE_ERROR -> onError()
                PlaybackStateCompat.STATE_PAUSED -> onPaused()
                PlaybackStateCompat.STATE_PLAYING -> onPlaying()
                PlaybackStateCompat.STATE_STOPPED -> onStopped()
            }
        })
    }

    private fun observerPlayerMetadata() {
        vm.playerMetadata.observe(viewLifecycleOwner, Observer {
            setTitle(it.mediaTitle);setSubTitle(it.mediaItemTitle)
            setDuration(it.mediaItemDuration);setSeekBarMax(it.mediaItemDurationMillis)
        })
    }

    private fun setSeekBarMax(max: Long) {
        if (max <= 0) return
        seekBar.max = max.toInt()
    }

    private fun setDuration(mediaItemDuration: String) {
        duration.text = mediaItemDuration
    }

    private fun setTitle(mediaTitle: String) {
        title.text = mediaTitle
    }

    private fun setSubTitle(mediaSubTitle: String) {
        subTitle.text = mediaSubTitle
    }

    private fun observerElapsedTime() {
        vm.elapsedTime.observe(viewLifecycleOwner, Observer {
            if (!isUserSeeking) {
                elapsedDuration.text = it.first
                seekBar.setProgressCompat(it.second)
            }
        })
    }

    private fun observerRepeatOneMode() {
        vm.repeatOne.observe(viewLifecycleOwner, Observer {
            if (it) repeatOne.toState1() else repeatOne.toState2()
        })
    }

    private fun observerShuffleMode() {
        vm.shuffle.observe(viewLifecycleOwner, Observer {
            if (it) shuffle.toState1() else shuffle.toState2()
        })
    }

    private fun onPlaying() {
        loading.hide()
        playPause.toState1()
    }

    private fun onPaused() {
        loading.hide()
        playPause.toState2()
    }

    private fun onError() {
        loading.hide()
        playPause.toState2()
    }

    private fun onBuffering() {
        loading.show()
        playPause.toState1()
    }

    private fun onStopped() {
        loading.hide()
        playPause.toState2()
    }

    private fun SeekBar.setProgressCompat(inProgress: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) seekBar.setProgress(
            inProgress.toInt(),
            true
        )
        else progress = inProgress.toInt()
    }

    private fun setSeekBarChangeListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar, progress: Int, isFromUser: Boolean) {
                if (isFromUser) {
                    isUserSeeking = true
                    elapsedDuration.text = progress.toLong().millisToPlayerDuration()
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar) {}
            override fun onStopTrackingTouch(seekbar: SeekBar) {
                isUserSeeking = false
                vm.seekTo(seekbar.progress)
            }
        })
    }

    fun playPause(mediaId: String) = vm.playPause(mediaId)

    fun previous() {
        previous.startAVDAnim()
        vm.previous()
    }

    fun next() {
        next.startAVDAnim()
        vm.next()
    }

    fun up() {
        (activity as? PlayerUpClickListener)?.onUp()
    }

}

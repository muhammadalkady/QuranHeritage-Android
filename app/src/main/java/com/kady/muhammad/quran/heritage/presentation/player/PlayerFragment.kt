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
import androidx.lifecycle.ViewModelProvider
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentPlayerBinding
import com.kady.muhammad.quran.heritage.domain.ext.millisToPlayerDuration
import com.kady.muhammad.quran.heritage.presentation.color.ColorViewModel
import com.kady.muhammad.quran.heritage.presentation.common.*
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class PlayerFragment : Fragment(), PanelSlideListener {

    private val vm: PlayerViewModel by lazy { ViewModelProvider(this).get(PlayerViewModel::class.java) }
    private val colorViewModel: ColorViewModel by lazy { (activity as MainActivity).colorViewModel }
    private val upLp = LinearLayout.LayoutParams(0, 0)
    private var isUserSeeking: Boolean = false
    lateinit var binding: FragmentPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.connectMediaBrowser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.up.layoutParams = upLp.apply {
            width = 0
            height = LinearLayout.LayoutParams.MATCH_PARENT
            weight = 1 - slideOffset
        }
        binding.up.translationX = slideOffset * binding.up.width
        binding.metadataContainerLinearLayout.alpha = slideOffset
    }

    private fun setupUp() {
        binding.upIcon.upAnimation()
    }

    private fun syncWithPanelLayout() {
        (activity as? PanelLayout)?.let {
            val panel: SlidingUpPanelLayout = it.getPanel()
            onPanelSlide(
                panel,
                if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F
            )
        }
    }

    private fun setBindingVariables() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.fragment = this
        binding.vm = vm
        binding.colorViewModel = colorViewModel
    }

    private fun observerPlayerState() {
        vm.playerState.observe(viewLifecycleOwner, {
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
        vm.playerMetadata.observe(viewLifecycleOwner, {
            setTitle(it.mediaTitle);setSubTitle(it.mediaItemTitle)
            setDuration(it.mediaItemDuration);setSeekBarMax(it.mediaItemDurationMillis)
        })
    }

    private fun setSeekBarMax(max: Long) {
        if (max <= 0) return
        binding.seekBar.max = max.toInt()
    }

    private fun setDuration(mediaItemDuration: String) {
        binding.duration.text = mediaItemDuration
    }

    private fun setTitle(mediaTitle: String) {
        binding.title.text = mediaTitle
    }

    private fun setSubTitle(mediaSubTitle: String) {
        binding.subTitle.text = mediaSubTitle
    }

    private fun observerElapsedTime() {
        vm.elapsedTime.observe(viewLifecycleOwner, {
            if (!isUserSeeking) {
                binding.elapsedDuration.text = it.first
                binding.seekBar.setProgressCompat(it.second)
            }
        })
    }

    private fun observerRepeatOneMode() {
        vm.repeatOne.observe(viewLifecycleOwner, {
            if (it) binding.repeatOne.toState1() else binding.repeatOne.toState2()
        })
    }

    private fun observerShuffleMode() {
        vm.shuffle.observe(viewLifecycleOwner, {
            if (it) binding.shuffle.toState1() else binding.shuffle.toState2()
        })
    }

    private fun onPlaying() {
        binding.loadingProgressBar.hide()
        binding.playPause.toState1()
    }

    private fun onPaused() {
        binding.loadingProgressBar.hide()
        binding.playPause.toState2()
    }

    private fun onError() {
        binding.loadingProgressBar.hide()
        binding.playPause.toState2()
    }

    private fun onBuffering() {
        binding.loadingProgressBar.show()
        binding.playPause.toState1()
    }

    private fun onStopped() {
        binding.loadingProgressBar.hide()
        binding.playPause.toState2()
    }

    private fun SeekBar.setProgressCompat(inProgress: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) binding.seekBar.setProgress(
            inProgress.toInt(),
            true
        )
        else progress = inProgress.toInt()
    }

    private fun setSeekBarChangeListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar, progress: Int, isFromUser: Boolean) {
                if (isFromUser) {
                    isUserSeeking = true
                    binding.elapsedDuration.text = progress.toLong().millisToPlayerDuration()
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
        binding.previous.startAVDAnim()
        vm.previous()
    }

    fun next() {
        binding.next.startAVDAnim()
        vm.next()
    }

    fun up() {
        (activity as? PlayerUpClickListener)?.onUp()
    }
}

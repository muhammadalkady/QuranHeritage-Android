package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.presentation.player.PlayerFragment
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    val vm by lazy { ViewModelProvider(this).get(MediaViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupSlidingPanel()
        setupLogo()
        panel.post { syncNavHostFragmentOffset(getPanelOffset()) }
    }

    override fun onBackPressed() {
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        syncNavHostFragmentOffset(slideOffset)
    }

    override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
    }

    fun playPause(mediaId: String) {
        playerFragment().playPause(mediaId)
    }

    private fun syncNavHostFragmentOffset(slideOffset: Float) {
        nav_host_fragment.translationY = -(nav_host_fragment.height * slideOffset)
        nav_host_fragment.alpha = 1 - slideOffset
        logo.alpha = slideOffset
    }

    private fun playerFragment(): PlayerFragment {
        return supportFragmentManager.findFragmentById(R.id.player) as PlayerFragment
    }

    private fun setupSlidingPanel() {
        panel.addPanelSlideListener(this)
    }

    private fun setupLogo() {
        logo.startAVDAnim()
    }

    private fun getPanelOffset() =
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F

}

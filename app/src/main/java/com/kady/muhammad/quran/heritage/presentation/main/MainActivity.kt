package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.presentation.ext.PanelLayout
import com.kady.muhammad.quran.heritage.presentation.ext.PanelSlideListener
import com.kady.muhammad.quran.heritage.presentation.ext.PlayerUpClickListener
import com.kady.muhammad.quran.heritage.presentation.player.PlayerFragment
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener, PlayerUpClickListener, PanelLayout {

    private val playerFragment: PlayerFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.player) as PlayerFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupSlidingPanel()
        panel.post {
            syncNavHostFragmentOffset(getPanelOffset())
            syncPlayerWithPanel(panel, getPanelOffset())
        }
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
        syncPlayerWithPanel(panel, slideOffset)
    }

    private fun syncPlayerWithPanel(panel: View, slideOffset: Float) {
        (playerFragment as? PanelSlideListener)?.onPanelSlide(panel, slideOffset)
    }

    override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
    }

    override fun onUp() {
        panel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun getPanel(): SlidingUpPanelLayout {
        return panel
    }

    fun playPause(mediaId: String) {
        playerFragment.playPause(mediaId)
    }

    private fun syncNavHostFragmentOffset(slideOffset: Float) {
        nav_host_fragment.translationY = -(nav_host_fragment.height * slideOffset)
        nav_host_fragment.alpha = 1 - slideOffset
        logo.alpha = slideOffset
    }

    private fun setupSlidingPanel() {
        panel.addPanelSlideListener(this)
    }

    private fun getPanelOffset() =
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F


}

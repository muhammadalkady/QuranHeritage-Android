package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.presentation.ext.PanelLayout
import com.kady.muhammad.quran.heritage.presentation.ext.PanelSlideListener
import com.kady.muhammad.quran.heritage.presentation.ext.PlayerUpClickListener
import com.kady.muhammad.quran.heritage.presentation.ext.animateHeight
import com.kady.muhammad.quran.heritage.presentation.media.MediaFragment
import com.kady.muhammad.quran.heritage.presentation.player.PlayerFragment
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player.*

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener,
    PlayerUpClickListener, PanelLayout {

    private val playerFragment: PlayerFragment by lazy {
        supportFragmentManager.findFragmentByTag("player") as PlayerFragment
    }
    private var isRestarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRestarted = savedInstanceState != null
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) addMediaFragment()
        panel.post {
            setupSlidingPanel()
            syncPlayerWithPanel(panel, getPanelOffset())
        }
        if (!isRestarted)
            fragmentContainer.animateHeight(1_000L)
    }

    override fun onBackPressed() {
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        syncPlayerWithPanel(panel, slideOffset)
    }

    private fun addMediaFragment() {
        replaceFragment(MediaFragment.newInstance(Const.MAIN_MEDIA_ID, null, null))
    }

    private fun replaceFragment(f: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, f).commit()
    }

    private fun syncPlayerWithPanel(panel: View, slideOffset: Float) {
        (playerFragment as? PanelSlideListener)?.onPanelSlide(panel, slideOffset)
    }

    override fun onPanelStateChanged(
        panel: View,
        previousState: SlidingUpPanelLayout.PanelState,
        newState: SlidingUpPanelLayout.PanelState
    ) {
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

    fun addFragmentToBackStack(f: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        fragmentTransaction.add(R.id.fragmentContainer, f).addToBackStack(null).commit()
    }


    private fun setupSlidingPanel() {
        panel.addPanelSlideListener(this)
        panel.setDragView(playerFragment.rootFrameLayout)
    }

    private fun getPanelOffset(): Float =
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F


}

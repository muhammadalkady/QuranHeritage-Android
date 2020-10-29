package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.presentation.search.SearchFragment
import com.kady.muhammad.quran.heritage.presentation.ext.*
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
        slidingUpPanelLayout.post {
            setupSlidingPanel()
            syncPlayerWithPanel(slidingUpPanelLayout, getPanelOffset())
        }
        if (!isRestarted) {
            fragmentContainerView.animateProperty(ViewProperty.HEIGHT, 1_000L)
        }
    }

    override fun onBackPressed() {
        if (slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        syncPlayerWithPanel(panel, slideOffset)
    }

    override fun onPanelStateChanged(
        panel: View,
        previousState: SlidingUpPanelLayout.PanelState,
        newState: SlidingUpPanelLayout.PanelState
    ) {
    }

    override fun onUp() {
        slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun getPanel(): SlidingUpPanelLayout {
        return slidingUpPanelLayout
    }

    private fun addMediaFragment() {
        replaceFragment(MediaFragment.newInstance(Const.MAIN_MEDIA_ID, null, null, false))
    }

    private fun replaceFragment(f: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, f).commit()
    }

    private fun syncPlayerWithPanel(panel: View, slideOffset: Float) {
        (playerFragment as? PanelSlideListener)?.onPanelSlide(panel, slideOffset)
    }

    private fun setupSlidingPanel() {
        slidingUpPanelLayout.addPanelSlideListener(this)
        slidingUpPanelLayout.setDragView(playerFragment.rootFrameLayout)
    }

    private fun getPanelOffset(): Float =
        if (slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F

    fun playPause(mediaId: String) {
        playerFragment.playPause(mediaId)
    }

    fun addFragmentToBackStack(f: Fragment, containerId: Int = R.id.fragmentContainerView) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        fragmentTransaction.add(containerId, f).addToBackStack(null).commit()
    }

    fun addSearchFragment(searchFragment: SearchFragment) {
        addFragmentToBackStack(searchFragment, R.id.searchFragmentContainer)
    }

    fun popSearchFragment() {
        supportFragmentManager.popBackStackImmediate()
    }

}

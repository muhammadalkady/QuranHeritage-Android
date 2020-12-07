package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.ActivityMainBinding
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.presentation.color.ColorFragment
import com.kady.muhammad.quran.heritage.presentation.color.ColorViewModel
import com.kady.muhammad.quran.heritage.presentation.common.PanelLayout
import com.kady.muhammad.quran.heritage.presentation.common.PanelSlideListener
import com.kady.muhammad.quran.heritage.presentation.common.PlayerUpClickListener
import com.kady.muhammad.quran.heritage.presentation.common.animateHeight
import com.kady.muhammad.quran.heritage.presentation.media.MediaFragment
import com.kady.muhammad.quran.heritage.presentation.player.PlayerFragment
import com.kady.muhammad.quran.heritage.presentation.search.SearchFragment
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener,
    PlayerUpClickListener, PanelLayout {

    val colorViewModel: ColorViewModel by lazy { ViewModelProvider(this).get(ColorViewModel::class.java) }
    private val playerFragment: PlayerFragment by lazy {
        supportFragmentManager.findFragmentByTag("player") as PlayerFragment
    }
    private var isRestarted = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRestarted = savedInstanceState != null
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.colorViewModel = colorViewModel
        if (savedInstanceState == null) addMediaFragment()
        binding.slidingUpPanelLayout.post {
            setupSlidingPanel()
            syncPlayerWithPanel(binding.slidingUpPanelLayout, getPanelOffset())
        }
        if (!isRestarted) {
            binding.mediaFragmentContainerView.animateHeight(duration = 1_000L)
        }
    }

    override fun onBackPressed() {
        if (binding.slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            binding.slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
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
        binding.slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun getPanel(): SlidingUpPanelLayout {
        return binding.slidingUpPanelLayout
    }

    private fun addMediaFragment() {
        replaceFragment(
            MediaFragment.newInstance(
                Const.MAIN_MEDIA_ID, null, null, false,
                preview = false
            )
        )
    }

    private fun replaceFragment(f: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mediaFragmentContainerView, f).commit()
    }

    private fun syncPlayerWithPanel(panel: View, slideOffset: Float) {
        (playerFragment as? PanelSlideListener)?.onPanelSlide(panel, slideOffset)
    }

    private fun setupSlidingPanel() {
        binding.slidingUpPanelLayout.addPanelSlideListener(this)
        binding.slidingUpPanelLayout.setDragView(playerFragment.binding.rootFrameLayout)
    }

    private fun getPanelOffset(): Float =
        if (binding.slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) 1F else 0F

    fun playPause(mediaId: String) {
        playerFragment.playPause(mediaId)
    }

    fun addFragmentToBackStack(f: Fragment, containerId: Int = R.id.mediaFragmentContainerView) {
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
        addFragmentToBackStack(searchFragment, R.id.rootFragmentContainer)
    }

    fun addColorFragment(colorFragment: ColorFragment) {
        addFragmentToBackStack(colorFragment, R.id.rootFragmentContainer)
    }

    fun popSearchFragment() {
        supportFragmentManager.popBackStackImmediate()
    }

}

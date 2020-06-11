package com.kady.muhammad.quran.heritage.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.presentation.player.PlayerFragment
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val vm by lazy { ViewModelProvider(this).get(MediaViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    fun playPause(mediaId: String) {
        playerFragment().playPause(mediaId)
    }

    private fun playerFragment(): PlayerFragment {
        return supportFragmentManager.findFragmentById(R.id.player) as PlayerFragment
    }

}

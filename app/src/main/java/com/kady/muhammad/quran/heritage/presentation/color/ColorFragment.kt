package com.kady.muhammad.quran.heritage.presentation.color

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentColorBinding
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.kady.muhammad.quran.heritage.presentation.media.MediaFragment
import com.madrapps.pikolo.listeners.OnColorSelectionListener

class ColorFragment : Fragment() {

    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }
    private val vm by lazy { mainActivity.colorViewModel }
    private lateinit var binding: FragmentColorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_color, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupColorPicker()
        if (savedInstanceState == null) addMediaFragment()
    }

    private fun addMediaFragment() {
        replaceFragment(
            MediaFragment.newInstance(
                Const.MAIN_MEDIA_ID, null, null,
                hideSearch = false,
                preview = true
            )
        )
    }

    private fun replaceFragment(f: Fragment) {
        val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mediaFragmentContainerView, f).commit()
    }

    private fun setupColorPicker() {
        binding.rgbColorPicker.setColorSelectionListener(object : OnColorSelectionListener {
            override fun onColorSelected(color: Int) {
                vm.primaryDarkColor.value = Color.darkenColor(color, .09F)
                vm.primaryColor.value = color
                vm.color1.value = color
                vm.color2.value = Color.lightenColor(color, .1F)
                if (ColorUtils.calculateLuminance(color) < .5F) {
                    //Dark color
                    vm.textPrimaryColor.value = Color.colorWhite
                    vm.textSecondaryColor.value = Color.darkenColor(Color.colorWhite, .2F)
                    vm.avdColor1.value = Color.colorWhite
                    vm.avdColor2.value = Color.darkenColor(Color.colorWhite, .8F)
                } else {
                    //Light color
                    vm.textPrimaryColor.value = Color.colorBlack
                    vm.textSecondaryColor.value = Color.lightenColor(Color.colorBlack, .2F)
                    vm.avdColor1.value = Color.colorBlack
                    vm.avdColor2.value = Color.lightenColor(Color.colorBlack, .8F)
                }

            }

            override fun onColorSelectionEnd(color: Int) {}
            override fun onColorSelectionStart(color: Int) {}
        })
    }

}
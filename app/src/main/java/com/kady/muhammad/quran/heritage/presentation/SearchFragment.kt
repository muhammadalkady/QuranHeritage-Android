package com.kady.muhammad.quran.heritage.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentSearchBinding
import com.kady.muhammad.quran.heritage.presentation.ext.*
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.kady.muhammad.quran.heritage.presentation.widget.AVDImageView

class SearchFragment : Fragment() {

    private val searchImageViewXPosition: Float by lazy {
        requireArguments().getFloat(
            SEARCH_ICON_IMAGE_VIEW_X_POSITION
        )
    }
    private val searchImageViewYPosition: Float by lazy {
        requireArguments().getFloat(
            SEARCH_ICON_IMAGE_VIEW_Y_POSITION
        )
    }
    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var binding: FragmentSearchBinding
    private var isRestarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRestarted = savedInstanceState != null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
    }

    fun onCloseImageViewClicked() {
        hideKeyboard()
        mainActivity.popSearchFragment()
    }

    private fun setupViews() {
        binding.rootSwipeLayout.disableSwipe = true
        if (!isRestarted) {
            animateSearchToCloseIcon()
            animateHeight()
            showKeyboard()
        } else {
            showCloseImageView()
        }
        binding.rootSwipeLayout.setDismissListener { hideKeyboard() }
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) hideKeyboard()
        }
    }

    private fun showKeyboard() {
        binding.searchEditText.requestFocus()
        binding.searchEditText.showKeyboard()
    }

    private fun hideKeyboard() {
        binding.searchEditText.hideKeyboard()
    }

    private fun animateHeight() {
        requireView().animateProperty(ViewProperty.HEIGHT)
    }

    private fun getContentView(): FrameLayout {
        return mainActivity.findViewById(android.R.id.content)
    }

    private fun animateSearchToCloseIcon() {
        binding.closeImageView.doOnLayout {
            val searchToCloseImageViewCopy = AVDImageView(requireContext())
            searchToCloseImageViewCopy.scaleType = ImageView.ScaleType.CENTER
            searchToCloseImageViewCopy.id = searchToCloseImageViewId
            //
            searchToCloseImageViewCopy.avd = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.search_to_close_avd_anim
            ) as AnimatedVectorDrawable
            getContentView().addView(searchToCloseImageViewCopy)
            searchToCloseImageViewCopy.apply {
                elevation = 20F.px
                layoutParams = layoutParams.apply {
                    width = binding.closeImageView.width;height = binding.closeImageView.height
                }
            }
            searchToCloseImageViewCopy.x = searchImageViewXPosition
            searchToCloseImageViewCopy.y = searchImageViewYPosition
            val xObjectAnimator: ObjectAnimator = ObjectAnimator
                .ofFloat(searchToCloseImageViewCopy, "x", binding.closeImageView.x)
            //
            val yObjectAnimator: ObjectAnimator = ObjectAnimator
                .ofFloat(searchToCloseImageViewCopy, "y", binding.closeImageView.y)
            //
            val animatorSet: AnimatorSet =
                AnimatorSet().setDuration(SEARCH_ICON_TO_CLOSE_ANIMATION_DURATION)
            animatorSet.playTogether(xObjectAnimator, yObjectAnimator)
            animatorSet.start()
            searchToCloseImageViewCopy.startAVDAnim()
            animatorSet.doOnEnd {
                showCloseImageView()
                getContentView().removeView(searchToCloseImageViewCopy)
            }
        }
        //
    }

    private fun showCloseImageView() {
        binding.closeImageView.alpha = 1F
    }

    companion object {
        val searchToCloseImageViewId = View.generateViewId()
        private const val SEARCH_ICON_TO_CLOSE_ANIMATION_DURATION = 700L
        private const val SEARCH_ICON_IMAGE_VIEW_X_POSITION = "x"
        private const val SEARCH_ICON_IMAGE_VIEW_Y_POSITION = "y"
        fun newInstance(
            searchImageViewXPosition: Float,
            searchImageViewYPosition: Float
        ): SearchFragment {
            return SearchFragment().apply {
                arguments = Bundle().apply {
                    putFloat(SEARCH_ICON_IMAGE_VIEW_X_POSITION, searchImageViewXPosition)
                    putFloat(SEARCH_ICON_IMAGE_VIEW_Y_POSITION, searchImageViewYPosition)
                }
            }
        }
    }
}
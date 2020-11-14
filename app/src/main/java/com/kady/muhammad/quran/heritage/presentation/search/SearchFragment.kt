package com.kady.muhammad.quran.heritage.presentation.search

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentSearchBinding
import com.kady.muhammad.quran.heritage.presentation.color.ColorViewModel
import com.kady.muhammad.quran.heritage.presentation.ext.*
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.kady.muhammad.quran.heritage.presentation.media.MediaFragment
import com.kady.muhammad.quran.heritage.presentation.widget.AVDImageView

class SearchFragment : Fragment() {

    private val adapter by lazy {
        SearchAdapter(
            resources.getInteger(R.integer.span_count),
            mutableListOf(),
            binding.searchResultRecyclerView,
            binding.rootHorizontalSwipeLayout,
            colorViewModel,
        )
    }
    private val searchImageViewXPosition: Int by lazy {
        requireArguments().getInt(
            SEARCH_ICON_IMAGE_VIEW_X_POSITION
        )
    }
    private val searchImageViewYPosition: Int by lazy {
        requireArguments().getInt(
            SEARCH_ICON_IMAGE_VIEW_Y_POSITION
        )
    }
    private val vm by lazy { ViewModelProvider(this).get(SearchViewModel::class.java) }
    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }
    private val colorViewModel: ColorViewModel by lazy { mainActivity.colorViewModel }
    private val viewHandler = Handler(Looper.getMainLooper())
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
        binding.vm = vm
        binding.colorViewModel = colorViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeSearch()
        observeSearchResult()
    }

    override fun onDestroyView() {
        viewHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    fun onCloseImageViewClicked() {
        hideKeyboard()
        mainActivity.popSearchFragment()
    }

    private fun observeSearchResult() {
        vm.searchResult.observe(
            viewLifecycleOwner,
            { adapter.updateMedia(it, vm.searchQuery.value ?: "") })
    }

    private fun observeSearch() {
        vm.searchQuery.observe(viewLifecycleOwner, { vm.search(it) })
    }

    private fun setupViews() {
        setupSearchResultRecyclerView()
        if (!isRestarted) handleFragmentFirstCreation() else showCloseImageView()
        setupSwipeLayout()
        hideKeyboardOnLossFocus()
    }

    private fun setupSearchResultRecyclerView() {
        context?.let {
            binding.searchResultRecyclerView.layoutManager =
                GridLayoutManager(it, resources.getInteger(R.integer.span_count))
            binding.searchResultRecyclerView.adapter = adapter
            binding.searchResultRecyclerView.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard()
                }
            })
        }
        adapter.setOnItemClickListener { mediaItem ->
            if (mediaItem.isList) {
                hideKeyboard()
                mainActivity
                    .addFragmentToBackStack(
                        MediaFragment
                            .newInstance(
                                mediaItem.id, "", mediaItem.title,
                                hideSearch = true,
                                preview = false
                            ),
                        R.id.rootFragmentContainer
                    )
            } else mainActivity.playPause(mediaItem.id)
        }
    }

    private fun handleFragmentFirstCreation() {
        animateSearchToCloseIcon()
        animateHeight()
        showKeyboard()
        binding.searchEditText.alpha = 0F
        binding.searchEditText.animate().alpha(1F)
            .setDuration(SEARCH_ICON_TO_CLOSE_ANIMATION_DURATION * 3).start()
    }

    private fun setupSwipeLayout() {
        binding.rootHorizontalSwipeLayout.addDismissListener { hideKeyboard() }
        binding.rootHorizontalSwipeLayout.addOnTouchUpListener { hideKeyboard() }
    }

    private fun hideKeyboardOnLossFocus() {
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) hideKeyboard() }
    }

    private fun showKeyboard() {
        viewHandler.postDelayed({
            binding.searchEditText.requestFocus()
            binding.searchEditText.showKeyboard()
        }, 700)
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
            searchToCloseImageViewCopy.tintDrawable(colorViewModel.avdColor1.value!!)
            //
            getContentView().addView(searchToCloseImageViewCopy)
            searchToCloseImageViewCopy.apply {
                post {
                    elevation = 20F.px
                    layoutParams = layoutParams.apply {
                        width = binding.closeImageView.width;height = binding.closeImageView.height
                    }
                }
            }
            searchToCloseImageViewCopy.x = searchImageViewXPosition.toFloat()
            searchToCloseImageViewCopy.y = searchImageViewYPosition.toFloat()
            val closeImageViewLocationOnScreen = IntArray(2)
            binding.closeImageView.getLocationInWindow(closeImageViewLocationOnScreen)
            val xObjectAnimator: ObjectAnimator = ObjectAnimator
                .ofFloat(
                    searchToCloseImageViewCopy,
                    "x",
                    closeImageViewLocationOnScreen[0].toFloat()
                )
            //
            val yObjectAnimator: ObjectAnimator = ObjectAnimator
                .ofFloat(
                    searchToCloseImageViewCopy,
                    "y",
                    closeImageViewLocationOnScreen[1].toFloat()
                )
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
            searchImageViewXPosition: Int,
            searchImageViewYPosition: Int
        ): SearchFragment {
            return SearchFragment().apply {
                arguments = Bundle().apply {
                    putInt(SEARCH_ICON_IMAGE_VIEW_X_POSITION, searchImageViewXPosition)
                    putInt(SEARCH_ICON_IMAGE_VIEW_Y_POSITION, searchImageViewYPosition)
                }
            }
        }
    }
}
package com.kady.muhammad.quran.heritage.presentation.media

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentMediaBinding
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.color.ColorFragment
import com.kady.muhammad.quran.heritage.presentation.common.animateHeight
import com.kady.muhammad.quran.heritage.presentation.common.hide
import com.kady.muhammad.quran.heritage.presentation.common.show
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.kady.muhammad.quran.heritage.presentation.search.SearchFragment
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModelFactory
import com.kady.muhammad.quran.heritage.presentation.widget.OptionMenu
import kotlinx.android.synthetic.main.fragment_media.*

class MediaFragment : Fragment() {

    private val logTag = "MediaFragment"
    private val mediaCountAnimationDuration = 150L
    private val adapter by lazy {
        MediaAdapter(
            requireContext(),
            resources.getInteger(R.integer.span_count),
            mutableListOf(),
            binding.mediaRecyclerView,
            binding.rootHorizontalSwipeLayout,
            mainActivity.colorViewModel
        )
    }

    private val vm by lazy {
        ViewModelProvider(
            this,
            MediaViewModelFactory(requireActivity().application, argParentMediaId)
        ).get(MediaViewModel::class.java)
    }

    private val argTitle: String by lazy {
        requireArguments().getString(Const.TITLE_KEY) ?: getString(R.string.main_title)
    }
    private val argParentMediaId: String by lazy { requireArguments().getString(Const.MEDIA_ID)!! }
    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }
    val preview: Boolean by lazy { requireArguments().getBoolean(Const.PREVIEW_KEY) }
    val hideSearch: Boolean by lazy { requireArguments().getBoolean(Const.HIDE_SEARCH_KEY) }

    private val updateImageViewRotateObjectAnimator: ObjectAnimator by lazy {
        ObjectAnimator
            .ofFloat(binding.updateImageView, "rotation", 0F, 360F)
            .apply {
                duration = 750
                repeatMode = ObjectAnimator.RESTART
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
    }

    private lateinit var binding: FragmentMediaBinding
    private var isRestarted = false
    private var animationEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRestarted = savedInstanceState != null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm
        binding.colorViewModel = mainActivity.colorViewModel
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSwipe()
        setToolbarTitle()
        setupToolbarLogo()
        setupMediaCount()
        setupUpdate()
        setupRecyclerView()
        observeLoading()
        observeMediaList()
        observeCount()
        observeColors()
        if (!isRestarted) animateAppBarLayoutHeight()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (!animationEnabled) {
            return object : Animation() {}.apply { duration = 0 }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    fun openSearchFragment() {
        //
        if (preview) return
        val searchImageViewLocationOnScreen = IntArray(2)
        binding.searchImageView.getLocationOnScreen(searchImageViewLocationOnScreen)
        val searchFragment: SearchFragment =
            SearchFragment.newInstance(
                searchImageViewLocationOnScreen[0],
                searchImageViewLocationOnScreen[1]
            )
        mainActivity.addSearchFragment(searchFragment)
    }

    fun showOptionMenu() {
        val optionMenu = OptionMenu(requireContext())
        val optionMenuItems: MutableList<OptionMenu.MenuItem> = mutableListOf()
        val colorsOptionMenuItem =
            OptionMenu.MenuItem(getString(R.string.colors), R.drawable.ic_outline_color_lens_24)
        val favoriteOptionMenuItem =
            OptionMenu.MenuItem(
                getString(R.string.favorite),
                R.drawable.ic_outline_favorite_border_24
            )
        val rateAppMenuItem =
            OptionMenu.MenuItem(getString(R.string.rate_app), R.drawable.ic_outline_star_rate_24)
        val aboutMenuItem =
            OptionMenu.MenuItem(getString(R.string.about_app), R.drawable.ic_outline_info_24)
        optionMenuItems.add(colorsOptionMenuItem)
        optionMenuItems.add(favoriteOptionMenuItem)
        optionMenuItems.add(rateAppMenuItem)
        optionMenuItems.add(aboutMenuItem)
        optionMenu.addMenuItems(optionMenuItems)
        optionMenu.show(binding.optionMenuImageView)
        optionMenu.addOnItemClickListener {
            when (it) {
                0 -> openColorFragment()
                1 -> openFavoriteFragment()
            }
        }
    }

    private fun openFavoriteFragment() {
        if (preview) return
    }

    private fun observeColors() {
        mainActivity.colorViewModel.primaryColor.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun animateAppBarLayoutHeight() {
        binding.appBarLayout.animateHeight()
    }

    private fun openColorFragment() {
        mainActivity.addColorFragment(ColorFragment())
    }

    private fun setupSwipe() {
        if (argParentMediaId == Const.MAIN_MEDIA_ID) rootHorizontalSwipeLayout.disableSwipe = true
        binding.rootHorizontalSwipeLayout.addDismissListener {
            animationEnabled = false
            requireActivity().supportFragmentManager.popBackStackImmediate()
            animationEnabled = true
        }
        binding.rootHorizontalSwipeLayout.addHorizontalSwipeListener { _, fraction ->
            val alpha = 1F - fraction
            binding.toolbar.alpha = alpha
        }
    }

    private fun setupUpdate() {
        binding.updateImageView.setOnClickListener { onUpdate() }
    }

    private fun onUpdate() {
        if (preview) return
        vm.getAllMedia()
    }

    private fun setupMediaCount() {
        showMediaCount()
    }

    private fun showMediaCount() {
        binding.mediaCountTextView.doOnLayout {
            binding.mediaCountTextView.translationY = binding.mediaCountTextView.height.toFloat()
            binding.mediaCountTextView.animate()
                .translationYBy(-binding.mediaCountTextView.height.toFloat())
                .setDuration(mediaCountAnimationDuration).start()
        }
    }

    private fun setupRecyclerView() {
        context?.let {
            binding.mediaRecyclerView.layoutManager =
                GridLayoutManager(it, resources.getInteger(R.integer.span_count))
            binding.mediaRecyclerView.adapter = adapter
        }
        if (!preview) {
            adapter.setOnItemClickListener { mediaItem ->
                if (mediaItem.isList) mainActivity
                    .addFragmentToBackStack(
                        newInstance(
                            mediaItem.id, argTitle, mediaItem.title, false,
                            preview = false
                        )
                    )
                else mainActivity.playPause(mediaItem.id)
            }
            //
        }
    }

    private fun setToolbarTitle() {
        binding.toolbarTitleTextView.text = argTitle
        binding.toolbarTitleTextView.isSelected = true
    }

    private fun setupToolbarLogo() {
        binding.appIconImageView.startAVDAnim()
    }

    private fun observeMediaList() {
        Logger.logI(logTag, "loadMediaList")
        vm.liveMedia
            .observe(viewLifecycleOwner,
                {
                    updateAdapter(it)
                    hideShowMediaRecyclerView(it.isNotEmpty())
                    showHideNoContent(it.isEmpty())
                })
    }

    private fun hideShowMediaRecyclerView(isShown: Boolean) {
        if (isShown) binding.mediaRecyclerView.show() else binding.mediaRecyclerView.hide()
    }

    private fun observeCount() {
        vm.liveMediaCount.observe(viewLifecycleOwner, {
            binding.mediaCountTextView.text = getString(R.string.media_count, it)
        })
    }

    private fun observeLoading() {
        vm.liveLoading.observe(viewLifecycleOwner, {
            binding.updateImageView.isEnabled = !it
            if (it) updateImageViewRotateObjectAnimator.start()
            else updateImageViewRotateObjectAnimator.cancel()
        })
    }

    private fun showHideNoContent(isShown: Boolean) {
        if (isShown) binding.noContentTextView.show() else binding.noContentTextView.hide()
    }

    private fun updateAdapter(childrenMedia: List<Media>) {
        context?.let { adapter.updateMedia(childrenMedia) }
    }

    companion object {
        fun newInstance(
            id: String,
            parentTitle: String?,
            title: String?,
            hideSearch: Boolean,
            preview: Boolean
        ): MediaFragment {
            val bundle = Bundle()
            bundle.putString(Const.MEDIA_ID, id)
            bundle.putBoolean(Const.HIDE_SEARCH_KEY, hideSearch)
            if (parentTitle != null && title != null) bundle.putString(
                Const.TITLE_KEY,
                "$parentTitle ● $title"
            )
            bundle.putBoolean(Const.PREVIEW_KEY, preview)
            val fragment = MediaFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

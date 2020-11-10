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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.FragmentMediaBinding
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.color.ColorFragment
import com.kady.muhammad.quran.heritage.presentation.search.SearchFragment
import com.kady.muhammad.quran.heritage.presentation.ext.ViewProperty
import com.kady.muhammad.quran.heritage.presentation.ext.animateProperty
import com.kady.muhammad.quran.heritage.presentation.ext.hide
import com.kady.muhammad.quran.heritage.presentation.ext.show
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModelFactory
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
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
    private val argTitle: String by lazy {
        requireArguments().getString(Const.TITLE_KEY) ?: getString(R.string.main_title)
    }
    private val vm by lazy {
        ViewModelProvider(
            this,
            MediaViewModelFactory(requireActivity().application, argParentMediaId)
        ).get(MediaViewModel::class.java)
    }

    val hideSearch: Boolean by lazy { requireArguments().getBoolean(Const.HIDE_SEARCH_KEY) }
    val argParentMediaId: String by lazy { requireArguments().getString(Const.MEDIA_ID)!! }

    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }

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
        binding.colorVm = mainActivity.colorViewModel
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSwipe()
        setToolbarTitle()
        setupToolbarLogo()
        setupToolbarMenu()
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
        val searchImageViewLocationOnScreen = IntArray(2)
        binding.searchImageView.getLocationOnScreen(searchImageViewLocationOnScreen)
        val searchFragment: SearchFragment =
            SearchFragment.newInstance(
                searchImageViewLocationOnScreen[0],
                searchImageViewLocationOnScreen[1]
            )
        mainActivity.addSearchFragment(searchFragment)
    }

    fun openFavoriteFragment() {

    }

    private fun observeColors() {
        val textColorPrimary: MutableLiveData<Int> = mainActivity.colorViewModel.textPrimaryColor
        mainActivity.colorViewModel.primaryColor.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
            binding.toolbar.overflowIcon!!.setTint(textColorPrimary.value!!)

        }
    }

    private fun animateAppBarLayoutHeight() {
        binding.appBarLayout.animateProperty(ViewProperty.HEIGHT)
    }

    private fun setupToolbarMenu() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.colors -> addColorFragment()
            }
            true
        }
    }

    private fun addColorFragment() {
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
        vm.mediaChildrenForParentId(false, argParentMediaId)
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
            binding.mediaRecyclerView.itemAnimator = SlideInDownAnimator()
        }
        adapter.setOnItemClickListener { mediaItem ->
            if (mediaItem.isList) mainActivity
                .addFragmentToBackStack(newInstance(mediaItem.id, argTitle, mediaItem.title, false))
            else mainActivity.playPause(mediaItem.id)
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
        ): MediaFragment {
            val bundle = Bundle()
            bundle.putString(Const.MEDIA_ID, id)
            bundle.putBoolean(Const.HIDE_SEARCH_KEY, hideSearch)
            if (parentTitle != null && title != null) bundle.putString(
                Const.TITLE_KEY,
                "$parentTitle ● $title"
            )
            val fragment = MediaFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

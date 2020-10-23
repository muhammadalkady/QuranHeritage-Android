package com.kady.muhammad.quran.heritage.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
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
            resources.getInteger(R.integer.span_count), mutableListOf()
        )
    }
    private val argParentMediaId: String by lazy { arguments?.getString("media-id")!! }
    private val argTitle: String by lazy {
        arguments?.getString("title") ?: getString(R.string.main_title)
    }
    private val vm by lazy {
        ViewModelProvider(
            this,
            MediaViewModelFactory(requireActivity().application, argParentMediaId)
        ).get(MediaViewModel::class.java)
    }

    private val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }

    private val mediaListScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val showMediaCount = (mediaRecyclerView.layoutManager as GridLayoutManager)
                .findFirstCompletelyVisibleItemPosition() == 0
            if (showMediaCount) showMediaCount() else hideMediaCount()
        }
    }
    private var animationEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSwipe()
        setToolbarTitle()
        setupToolbarLogo()
        setupMediaCount()
        setupUpdate()
        initList()
        observeLoading()
        observeMediaList()
        observeCount()
    }

    override fun onDestroyView() {
        mediaRecyclerView.removeOnScrollListener(mediaListScrollListener)
        super.onDestroyView()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (!animationEnabled) {
            return object : Animation() {}.apply { duration = 0 }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    private fun setupSwipe() {
        if (argParentMediaId == Const.MAIN_MEDIA_ID) rootConstraintLayout.disableSwipe = true
        rootConstraintLayout.setDismissListener {
            animationEnabled = false
            requireActivity().supportFragmentManager.popBackStackImmediate()
            animationEnabled = true
        }
        rootConstraintLayout.setSwipeListener {
            toolbarTitleTextView.alpha = 1F - it
            updateTextView.alpha = 1F - it
            Logger.logI(logTag, it.toString(), false)
        }
    }

    private fun setupUpdate() {
        updateTextView.setOnClickListener { onUpdate() }
    }

    private fun onUpdate() {
        vm.mediaChildrenForParentId(false, argParentMediaId)
    }

    private fun setupMediaCount() {
        showMediaCount()
        mediaRecyclerView.addOnScrollListener(mediaListScrollListener)
    }

    private fun showMediaCount() {
        mediaCountTextView.doOnLayout {
            mediaCountTextView.translationY = mediaCountTextView.height.toFloat()
            mediaCountTextView.animate().translationYBy(-mediaCountTextView.height.toFloat())
                .setDuration(mediaCountAnimationDuration).start()
        }
    }

    private fun hideMediaCount() {
        mediaCountTextView.doOnLayout {
            mediaCountTextView.animate().translationYBy(mediaCountTextView.height.toFloat())
                .setDuration(mediaCountAnimationDuration).start()
        }
    }

    private fun initList() {
        context?.let {
            mediaRecyclerView.layoutManager =
                GridLayoutManager(it, resources.getInteger(R.integer.span_count))
            mediaRecyclerView.adapter = adapter
            mediaRecyclerView.itemAnimator = SlideInDownAnimator()
        }
        adapter.setOnItemClickListener { mediaItem ->
            if (mediaItem.isList) mainActivity
                .addFragmentToBackStack(newInstance(mediaItem.id, argTitle, mediaItem.title))
            else mainActivity.playPause(mediaItem.id)
        }
    }

    private fun setToolbarTitle() {
        toolbarTitleTextView.text = argTitle
        toolbarTitleTextView.isSelected = true
    }

    private fun setupToolbarLogo() {
        appIconImageView.startAVDAnim()
    }

    private fun observeMediaList() {
        Logger.logI(logTag, "loadMediaList")
        vm.liveMedia
            .observe(viewLifecycleOwner,
                { updateAdapter(it);showNoContent(it.isEmpty()) })
    }

    private fun observeCount() {
        vm.liveMediaCount.observe(viewLifecycleOwner, {
            mediaCountTextView.text = getString(R.string.media_count, it)
        })
    }

    private fun observeLoading() {
        vm.liveLoading.observe(viewLifecycleOwner, {
            if (it) {
                loadingProgressBar.show()
                updateTextView.isEnabled = false
            } else {
                loadingProgressBar.hide()
                updateTextView.isEnabled = true
            }
        })
    }

    private fun showNoContent(isShown: Boolean) {
        if (isShown) noContentTextView.show() else noContentTextView.hide()
    }

    private fun updateAdapter(childrenMedia: List<Media>) {
        context?.let { adapter.updateMedia(childrenMedia) }
    }

    companion object {
        fun newInstance(id: String, parentTitle: String?, title: String?): MediaFragment {
            val bundle = Bundle()
            bundle.putString(Const.MEDIA_ID, id)
            if (parentTitle != null && title != null) bundle.putString(
                "title",
                "$parentTitle ● $title"
            )
            val fragment = MediaFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

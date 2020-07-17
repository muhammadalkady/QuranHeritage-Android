package com.kady.muhammad.quran.heritage.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.ext.hide
import com.kady.muhammad.quran.heritage.presentation.ext.show
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModelFactory
import kotlinx.android.synthetic.main.fragment_media.*

class MediaFragment : Fragment() {

    private val logTag = "MediaFragment"
    private val animationDuration = 150L
    private val adapter by lazy { MediaAdapter(requireContext(), resources.getInteger(R.integer.span_count), argTitle, mutableListOf()) }
    private val argParentMediaId: String by lazy { arguments?.getString("media-id")!! }
    private val argTitle: String by lazy { arguments?.getString("title") ?: getString(R.string.main_title) }
    private val vm by lazy {
        ViewModelProvider(this, MediaViewModelFactory(requireActivity().application, argParentMediaId)).get(MediaViewModel::class.java)
    }
    private val mediaListScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val showMediaCount = (mediaList.layoutManager as GridLayoutManager)
                .findFirstCompletelyVisibleItemPosition() == 0
            if (showMediaCount) showMediaCount() else hideMediaCount()
        }
    }
    private var animationEnabled = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        mediaList.removeOnScrollListener(mediaListScrollListener)
        super.onDestroyView()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (!animationEnabled) {
            return object : Animation() {}.apply { duration = 0 }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    private fun setupSwipe() {
        if (argParentMediaId == Const.MAIN_MEDIA_ID) root.disableSwipe = true
        root.setDismissListener {
            animationEnabled = false
            requireActivity().supportFragmentManager.popBackStackImmediate()
            animationEnabled = true
        }
    }

    private fun setupUpdate() {
        update.setOnClickListener { onUpdate() }
    }

    private fun onUpdate() {
        vm.mediaChildrenForParentId(false, argParentMediaId)
    }

    private fun setupMediaCount() {
        showMediaCount()
        mediaList.addOnScrollListener(mediaListScrollListener)
    }

    private fun showMediaCount() {
        mediaCount.doOnLayout {
            mediaCount.translationY = mediaCount.height.toFloat()
            mediaCount.animate().translationYBy(-mediaCount.height.toFloat()).setDuration(animationDuration).start()
        }
    }

    private fun hideMediaCount() {
        mediaCount.doOnLayout {
            mediaCount.animate().translationYBy(mediaCount.height.toFloat()).setDuration(animationDuration).start()
        }
    }

    private fun initList() {
        context?.let {
            mediaList.layoutManager = GridLayoutManager(it, resources.getInteger(R.integer.span_count))
            mediaList.adapter = adapter
        }
    }

    private fun setToolbarTitle() {
        title.text = argTitle
        title.isSelected = true
    }

    private fun setupToolbarLogo() {
        background.startAVDAnim()
    }

    private fun observeMediaList() {
        Logger.logI(logTag, "loadMediaList")
        vm.liveMedia
            .observe(viewLifecycleOwner,
                Observer { updateAdapter(it);showNoContent(it.isEmpty()) })
    }

    private fun observeCount() {
        vm.liveMediaCount.observe(viewLifecycleOwner, Observer {
            mediaCount.text = getString(R.string.media_count, it)
        })
    }

    private fun observeLoading() {
        vm.liveLoading.observe(viewLifecycleOwner, Observer {
            if (it) loading.show()
            else loading.hide()
        })
    }

    private fun showNoContent(isShown: Boolean) {
        if (isShown) noContent.show() else noContent.hide()
    }

    private fun updateAdapter(childrenMedia: List<Media>) {
        context?.let { adapter.updateMedia(childrenMedia) }
    }

    companion object {
        fun newInstance(id: String, parentTitle: String?, title: String?): MediaFragment {
            val bundle = Bundle()
            bundle.putString(Const.MEDIA_ID, id)
            if (parentTitle != null && title != null) bundle.putString("title", "$parentTitle ● $title")
            val fragment = MediaFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

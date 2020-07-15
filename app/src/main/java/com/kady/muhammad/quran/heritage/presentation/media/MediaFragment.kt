package com.kady.muhammad.quran.heritage.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.ext.hide
import com.kady.muhammad.quran.heritage.presentation.ext.show
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModel
import com.kady.muhammad.quran.heritage.presentation.vm.MediaViewModelFactory
import kotlinx.android.synthetic.main.fragment_media.*


class MediaFragment : Fragment() {

    private val logTag = "MediaFragment"
    private val animationDuration = 150L
    private val adapter by lazy { MediaAdapter(requireContext(), resources.getInteger(R.integer.span_count), mTitle, mutableListOf()) }
    private val parentMediaId: String by lazy { arguments?.getString("media-id")!! }
    private val mTitle: String by lazy { arguments?.getString("title") ?: getString(R.string.main_title) }
    private val vm by lazy {
        ViewModelProvider(this, MediaViewModelFactory(requireActivity().application, parentMediaId)).get(MediaViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolbarTitle(mTitle)
        setupToolbarLogo()
        setupMediaCount()
        setupUpdate()
        initList()
        observeLoading()
        observeMediaList()
        observeCount()
    }

    private fun setupUpdate() {
        update.setOnClickListener { onUpdate() }
    }

    private fun onUpdate() {
        vm.mediaChildrenForParentId(false, parentMediaId)
    }

    private fun setupMediaCount() {
        showMediaCount()
        mediaRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val showMediaCount = (mediaRecyclerView.layoutManager as GridLayoutManager)
                    .findFirstCompletelyVisibleItemPosition() == 0
                if (showMediaCount) showMediaCount() else hideMediaCount()
            }
        })
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
            mediaRecyclerView.layoutManager = GridLayoutManager(it, resources.getInteger(R.integer.span_count))
            mediaRecyclerView.adapter = adapter
        }
    }

    private fun setToolbarTitle(intTitle: String) {
        title.text = intTitle
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
}

package com.kady.muhammad.quran.heritage.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
    private val adapter by lazy { MediaAdapter(requireContext(), resources.getInteger(R.integer.span_count), mTitle, mutableListOf()) }
    private val parentMediaId: String by lazy { arguments?.getString("media-id")!! }
    private val mTitle: String by lazy { arguments?.getString("title") ?: getString(R.string.main_title) }
    private val vm by lazy {
        ViewModelProvider(
            this, MediaViewModelFactory(
                requireActivity().application,
                parentMediaId
            )
        ).get(MediaViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolbarTitle(mTitle)
        setupToolbarLogo()
        initPullToRefresh()
        initList()
        observeMediaList()
    }

    private fun initPullToRefresh() {
        srl.setOnRefreshListener { onRefresh() }
    }

    private fun onRefresh() {
        vm.mediaChildrenForParentId(false, parentMediaId)
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
        logo.startAVDAnim()
    }

    private fun observeMediaList() {
        Logger.logI(logTag, "loadMediaList")
        loading.show()
        vm.liveMedia
            .observe(viewLifecycleOwner,
                Observer { updateAdapter(it);showNoContent(it.isEmpty()) })
    }

    private fun showNoContent(isShown: Boolean) {
        if (isShown) noContent.show() else noContent.hide()
    }

    private fun updateAdapter(childrenMedia: List<Media>) {
        context?.let { adapter.updateMedia(childrenMedia);srl.isRefreshing = false;loading.hide() }
    }
}

package com.kady.muhammad.quran.heritage.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.ext.hide
import com.kady.muhammad.quran.heritage.presentation.ext.show
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import kotlinx.android.synthetic.main.fragment_media.*

class MediaFragment : Fragment() {

    private val logTag = "MediaFragment"
    private val adapter by lazy { MediaAdapter(requireContext(), resources.getInteger(R.integer.span_count), mTitle, mutableListOf()) }
    private val parentMediaId: String by lazy { arguments?.getString("media-id")!! }
    private val mTitle: String by lazy { arguments?.getString("title") ?: getString(R.string.main_title) }
    private val vm by lazy { (activity as MainActivity).vm }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolbarTitle(mTitle)
        setupToolbarLogo()
        initPullToRefresh()
        initList()
        loadMediaList()
    }

    private fun initPullToRefresh() {
        if (parentMediaId != Const.MAIN_MEDIA_ID) srl.isEnabled = false
        srl.setOnRefreshListener { onRefresh() }
    }

    private fun onRefresh() {
        loadMediaList(true)
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

    private fun loadMediaList(force: Boolean = false) {
        Logger.logI(logTag, "loadMediaList")
        if (parentMediaId == Const.MAIN_MEDIA_ID) srl.isRefreshing = true
        vm.mediaChildrenForParentId(parentMediaId, force)
            .observe(viewLifecycleOwner,
                Observer { updateAdapter(it);srl.isRefreshing = false;showNoContent(it.isEmpty()) })
    }

    private fun showNoContent(isShown: Boolean) {
        if (isShown) noContent.show() else noContent.hide()
    }

    private fun updateAdapter(childrenMedia: List<Media>) {
        context?.let { adapter.updateMedia(childrenMedia);srl.isRefreshing = false }
    }
}

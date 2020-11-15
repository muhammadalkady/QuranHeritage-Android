package com.kady.muhammad.quran.heritage.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.SeachItemListTypeBinding
import com.kady.muhammad.quran.heritage.databinding.SearchItemNonListTypeBinding
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.color.ColorViewModel
import com.kady.muhammad.quran.heritage.presentation.widget.HorizontalSwipeLayout
import com.kady.muhammad.quran.heritage.presentation.widget.SwipeRecyclerView
import kotlinx.android.synthetic.main.media_item.view.*

class SearchAdapter(
    private val spanCount: Int,
    private val mediaList: MutableList<Media> = mutableListOf(),
    private val recyclerView: SwipeRecyclerView,
    private val parentHorizontalSwipeLayout: HorizontalSwipeLayout,
    private val colorViewModel: ColorViewModel,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //
    var listener: ((mediaItem: Media) -> Unit)? = null
    var query: String = ""

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mediaItem: Media = mediaList[holder.adapterPosition]
        if (holder is MediaListTypeHolder)
            holder.bind(mediaItem, position)
        else if (holder is MediaNonListTypeHolder) holder.bind(mediaItem, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val li = LayoutInflater.from(context)
        return if (viewType == LIST_TYPE)
            MediaListTypeHolder(
                DataBindingUtil.inflate(
                    li,
                    R.layout.seach_item_list_type,
                    parent,
                    false
                )
            )
        else MediaNonListTypeHolder(
            DataBindingUtil.inflate(
                li,
                R.layout.search_item_non_list_type,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        val media = mediaList[position]
        return if (media.isList) LIST_TYPE else NON_LIST_TYPE
    }

    fun updateMedia(mediaList: List<Media>, query: String) {
        this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
        this.query = query
    }

    fun setOnItemClickListener(listener: (mediaItem: Media) -> Unit) {
        this.listener = listener
    }

    inner class MediaListTypeHolder(private val binding: SeachItemListTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val horizontalSwipeLayout: HorizontalSwipeLayout =
            binding.root.horizontalSwipeLayout
        private val horizontalSwipeLayoutTag = "${horizontalSwipeLayout.tag}"

        init {
            binding.spanCount = spanCount
            binding.colorViewModel = colorViewModel
            //
            horizontalSwipeLayout.setUpWithRecyclerView(recyclerView, horizontalSwipeLayoutTag)
            horizontalSwipeLayout.setupWithParentHorizontalSwipeLayout(parentHorizontalSwipeLayout)
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.query = query
            binding.executePendingBindings()
            binding.root.horizontalSwipeLayout.setOnClickListener { listener?.invoke(mediaItem) }
            binding.root.horizontalSwipeLayoutActions.setOnClickListener { horizontalSwipeLayout.swipeBack() }
        }
    }

    inner class MediaNonListTypeHolder(private val binding: SearchItemNonListTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val horizontalSwipeLayout: HorizontalSwipeLayout =
            binding.root.horizontalSwipeLayout
        private val horizontalSwipeLayoutTag = "${horizontalSwipeLayout.tag}"

        init {
            binding.spanCount = spanCount
            binding.colorViewModel = colorViewModel
            horizontalSwipeLayout.setUpWithRecyclerView(recyclerView, horizontalSwipeLayoutTag)
            horizontalSwipeLayout.setupWithParentHorizontalSwipeLayout(parentHorizontalSwipeLayout)
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.query = query
            binding.executePendingBindings()
            binding.root.horizontalSwipeLayout.setOnClickListener { listener?.invoke(mediaItem) }
            binding.root.horizontalSwipeLayoutActions.setOnClickListener { horizontalSwipeLayout.swipeBack() }
        }

    }

    companion object {
        private const val LIST_TYPE = 0
        private const val NON_LIST_TYPE = 1
    }
}
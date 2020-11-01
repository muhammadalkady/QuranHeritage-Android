package com.kady.muhammad.quran.heritage.presentation.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.SeachItemListTypeBinding
import com.kady.muhammad.quran.heritage.databinding.SearchItemNonListTypeBinding
import com.kady.muhammad.quran.heritage.entity.media.Media

class SearchAdapter(
    private val context: Context,
    private val spanCount: Int,
    private val mediaList: MutableList<Media> = mutableListOf(),
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        if (this.mediaList.isEmpty()) {
            this.mediaList.addAll(mediaList)
            notifyItemRangeChanged(0, mediaList.size)
        } else {
            this.mediaList.clear()
            this.mediaList.addAll(mediaList)
            notifyDataSetChanged()
        }
        this.query = query
    }

    fun setOnItemClickListener(listener: (mediaItem: Media) -> Unit) {
        this.listener = listener
    }

    inner class MediaListTypeHolder(private val binding: SeachItemListTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.spanCount = spanCount
            binding.drawable1 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_1)
            binding.drawable2 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_2)
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.query = query
            binding.executePendingBindings()
            binding.root.setOnClickListener { listener?.invoke(mediaItem) }
        }
    }

    inner class MediaNonListTypeHolder(private val binding: SearchItemNonListTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.spanCount = spanCount
            binding.drawable1 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_1)
            binding.drawable2 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_2)
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.query = query
            binding.executePendingBindings()
            binding.root.setOnClickListener { listener?.invoke(mediaItem) }
        }

    }

    companion object {
        private const val LIST_TYPE = 0
        private const val NON_LIST_TYPE = 1
    }
}
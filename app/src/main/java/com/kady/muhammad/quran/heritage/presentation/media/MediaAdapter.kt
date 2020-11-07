package com.kady.muhammad.quran.heritage.presentation.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.MediaItemBinding
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.widget.SwipeRecyclerView
import kotlinx.android.synthetic.main.media_item.view.*

class MediaAdapter(
    private val context: Context,
    private val spanCount: Int,
    private val mediaList: MutableList<Media> = mutableListOf(),
    private val recyclerView: SwipeRecyclerView,
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    var listener: ((mediaItem: Media) -> Unit)? = null

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaHolder, position: Int) {
        val mediaItem: Media = mediaList[holder.adapterPosition]
        holder.bind(mediaItem, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaHolder {
        val context = parent.context
        val li = LayoutInflater.from(context)
        return MediaHolder(DataBindingUtil.inflate(li, R.layout.media_item, parent, false))
    }

    fun updateMedia(mediaList: List<Media>) {
        if (this.mediaList.isEmpty()) {
            this.mediaList.addAll(mediaList)
            notifyItemRangeChanged(0, mediaList.size)
        } else {
            this.mediaList.clear()
            this.mediaList.addAll(mediaList)
            notifyDataSetChanged()
        }
    }

    fun setOnItemClickListener(listener: (mediaItem: Media) -> Unit) {
        this.listener = listener
    }

    inner class MediaHolder(private val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.spanCount = spanCount
            binding.drawable1 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_1)
            binding.drawable2 =
                ContextCompat.getDrawable(context, R.drawable.media_item_background_2)
        }

        fun bind(mediaItem: Media, position: Int) {
            val horizontalSwipeLayout = binding.root.horizontalSwipeLayout
            //
            binding.mediaItem = mediaItem
            binding.position = position
            binding.executePendingBindings()
            horizontalSwipeLayout.setOnClickListener { listener?.invoke(mediaItem) }
            horizontalSwipeLayout.setUpWithRecyclerView(
                recyclerView,
                "${horizontalSwipeLayout.tag}"
            )
        }

    }
}
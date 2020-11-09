package com.kady.muhammad.quran.heritage.presentation.media

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.MediaItemBinding
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.widget.HorizontalSwipeLayout
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

    private val swipedMap: MutableMap<Int, String> = mutableMapOf()

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
        RecyclerView.ViewHolder(binding.root), HorizontalSwipeLayout.HorizontalSwipeListener {

        private val horizontalSwipeLayout = binding.root.horizontalSwipeLayout
        private val horizontalSwipeLayoutTag = "${horizontalSwipeLayout.tag}"
        private val horizontalSwipeLayoutActions = binding.root.horizontalSwipeLayoutActions

        init {
            //
            binding.spanCount = spanCount
            binding.drawable1 = getDrawable(R.drawable.media_item_background_1)
            binding.drawable2 = getDrawable(R.drawable.media_item_background_2)
            //
            horizontalSwipeLayout.setUpWithRecyclerView(recyclerView, horizontalSwipeLayoutTag)
            horizontalSwipeLayout.addHorizontalSwipeListener(this)
        }

        private fun getDrawable(drawableRes: Int): Drawable? {
            return ContextCompat.getDrawable(context, drawableRes)
        }

        private fun setSwipe(isSwiped: Boolean) {
            with(horizontalSwipeLayout) { if (isSwiped) toMaxSwipe(false) else swipeBack(false) }
        }

        fun bind(mediaItem: Media, position: Int) {
            //
            binding.mediaItem = mediaItem
            binding.position = position
            binding.executePendingBindings()
            with(horizontalSwipeLayout) {
                setOnClickListener { listener?.invoke(mediaItem) }
                val isSwiped = swipedMap[adapterPosition]
                setSwipe(isSwiped != null)
            }
            horizontalSwipeLayoutActions.setOnClickListener { horizontalSwipeLayout.swipeBack() }
        }

        override fun onHorizontalSwipe(
            horizontalSwipeLayout: HorizontalSwipeLayout,
            fraction: Float
        ) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            if (fraction == 1F) {
                val mediaId = mediaList[adapterPosition].id
                swipedMap[adapterPosition] = mediaId
            } else if (fraction < 1F) {
                swipedMap.remove(adapterPosition)
            }
        }

    }
}
package com.kady.muhammad.quran.heritage.presentation.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.MediaItemBinding
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.color.ColorViewModel
import com.kady.muhammad.quran.heritage.presentation.common.MediaAdapterHelper
import com.kady.muhammad.quran.heritage.presentation.widget.HorizontalSwipeLayout
import com.kady.muhammad.quran.heritage.presentation.widget.SwipeRecyclerView
import kotlinx.android.synthetic.main.media_item.view.*

class MediaAdapter(
    context: Context,
    private val spanCount: Int,
    private val mediaList: MutableList<Media> = mutableListOf(),
    private val recyclerView: SwipeRecyclerView,
    private val parentHorizontalSwipeLayout: HorizontalSwipeLayout,
    private val colorViewModel: ColorViewModel,
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    var mediaClickListener: ((mediaItem: Media) -> Unit)? = null
    var favoriteClickListener: ((mediaItem: Media, horizontalSwipeLayout: HorizontalSwipeLayout) -> Unit)? =
        null

    private val mediaAdapterHelper = MediaAdapterHelper(context, mediaList)

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

    override fun onViewAttachedToWindow(holder: MediaHolder) {
        holder.itemView.clearAnimation()
    }

    fun updateMedia(mediaList: List<Media>) {
        this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(mediaClickListener: (mediaItem: Media) -> Unit) {
        this.mediaClickListener = mediaClickListener
    }

    fun setOnFavoriteClickListener(favoriteClickListener: (mediaItem: Media, horizontalSwipeLayout: HorizontalSwipeLayout) -> Unit) {
        this.favoriteClickListener = favoriteClickListener
    }

    inner class MediaHolder(private val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root), HorizontalSwipeLayout.HorizontalSwipeListener {

        private val horizontalSwipeLayout = binding.root.horizontalSwipeLayout
        private val horizontalSwipeLayoutTag = "${horizontalSwipeLayout.tag}"
        private val horizontalSwipeLayoutActions = binding.root.horizontalSwipeLayoutActions

        init {
            //
            binding.spanCount = spanCount
            binding.colorViewModel = colorViewModel
            //
            horizontalSwipeLayout.setUpWithRecyclerView(recyclerView, horizontalSwipeLayoutTag)
            horizontalSwipeLayout.setupWithParentHorizontalSwipeLayout(parentHorizontalSwipeLayout)
            horizontalSwipeLayout.addHorizontalSwipeListener(this)
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.spanCount = spanCount
            binding.colorViewModel = colorViewModel
            binding.executePendingBindings()
            with(horizontalSwipeLayout) {
                setOnClickListener { mediaClickListener?.invoke(mediaItem) }
                mediaAdapterHelper.restoreHorizontalSwipeLayoutState(
                    adapterPosition,
                    horizontalSwipeLayout
                )
            }
            horizontalSwipeLayoutActions.setOnClickListener {
                favoriteClickListener?.invoke(mediaItem, horizontalSwipeLayout)
            }
        }

        override fun onHorizontalSwipe(
            horizontalSwipeLayout: HorizontalSwipeLayout,
            fraction: Float
        ) {
            mediaAdapterHelper.onHorizontalSwipe(adapterPosition, fraction)
        }
    }

}
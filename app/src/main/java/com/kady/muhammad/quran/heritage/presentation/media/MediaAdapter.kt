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

class MediaAdapter(
    private val context: Context,
    private val spanCount: Int,
    private val mediaList: MutableList<Media> = mutableListOf()
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    var listener: ((mediaItem: Media) -> Unit)? = null
    private val primaryColor by lazy { ContextCompat.getColor(context, R.color.colorPrimary) }
    private val primaryColorDark by lazy {
        ContextCompat.getColor(
            context,
            R.color.colorPrimaryDark
        )
    }

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
        if (this.mediaList.isNotEmpty()) this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (mediaItem: Media) -> Unit) {
        this.listener = listener
    }

    inner class MediaHolder(private val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.spanCount = spanCount
            binding.color1 = primaryColorDark
            binding.color2 = primaryColor
        }

        fun bind(mediaItem: Media, position: Int) {
            binding.mediaItem = mediaItem
            binding.position = position
            binding.executePendingBindings()
            binding.root.setOnClickListener { listener?.invoke(mediaItem) }
        }

    }
}
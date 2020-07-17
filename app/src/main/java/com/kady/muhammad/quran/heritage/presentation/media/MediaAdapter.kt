package com.kady.muhammad.quran.heritage.presentation.media

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import kotlinx.android.synthetic.main.media_item.view.*

class MediaAdapter(
    private val context: Context,
    private val spanCount: Int,
    private val parentTitle: String,
    private val mediaList: MutableList<Media> = mutableListOf()
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    private val primaryColor by lazy { ContextCompat.getColor(context, R.color.colorPrimary) }
    private val primaryColorDark by lazy { ContextCompat.getColor(context, R.color.colorPrimaryDark) }

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaHolder, position: Int) {
        //Set background color
        val background = if (spanCount.rem(2) != 0) {
            if (position.rem(2) == 0) primaryColorDark else primaryColor
        } else {
            val positionToSumWith = if (position.rem(2) == 0) position + 1 else position - 1
            if (((position + positionToSumWith) - 1).rem(8) == 0) primaryColorDark else primaryColor
        }
        val mediaItem = mediaList[holder.adapterPosition]
        with(holder.itemView) {
            setBackgroundColor(background)
            title.text = mediaItem.title
            if (mediaItem.isList) avatar.setImageResource(R.drawable.ic_playlist)
            else avatar.setImageResource(R.drawable.ic_track)
            setOnClickListener {
                val ma = holder.itemView.context as MainActivity
                if (mediaItem.isList) {
                    ma.addFragmentToBackStack(MediaFragment.newInstance(mediaItem.id, parentTitle, mediaItem.title))
                } else {
                    ma.playPause(mediaItem.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaHolder =
        MediaHolder(LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false))

    fun updateMedia(mediaList: List<Media>) {
        if (this.mediaList.isNotEmpty()) this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
    }

    class MediaHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
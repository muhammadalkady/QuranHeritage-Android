package com.kady.muhammad.quran.heritage.presentation.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import kotlinx.android.synthetic.main.media_item.view.*

class MediaAdapter(
    private val parentTitle: String,
    private val mediaList: MutableList<Media> = mutableListOf()
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaHolder, position: Int) {
        val mediaItem = mediaList[holder.adapterPosition]
        with(holder.itemView) {
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
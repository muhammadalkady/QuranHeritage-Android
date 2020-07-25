package com.kady.muhammad.quran.heritage.presentation.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import kotlinx.android.synthetic.main.media_item.view.*

class MediaAdapter(
    private val mainActivity: MainActivity,
    private val parentTitle: String,
    private val mediaList: MutableList<Media> = mutableListOf()
) :
    RecyclerView.Adapter<MediaAdapter.MediaHolder>() {

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaHolder, position: Int) {
        val mediaItem: Media = mediaList[holder.adapterPosition]
        with(holder) {
            setTitle(mediaItem)
            setAvatar(mediaItem)
            setItemClick(mediaItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaHolder =
        MediaHolder(LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false))

    private fun MediaHolder.setItemClick(mediaItem: Media) {
        itemView.setOnClickListener {
            if (mediaItem.isList) mainActivity
                .addFragmentToBackStack(
                    MediaFragment
                        .newInstance(mediaItem.id, parentTitle, mediaItem.title)
                )
            else mainActivity.playPause(mediaItem.id)
        }
    }

    private fun MediaHolder.setAvatar(mediaItem: Media) {
        if (mediaItem.isList) avatar.setImageResource(R.drawable.ic_playlist)
        else avatar.setImageResource(R.drawable.ic_track)
    }

    private fun MediaHolder.setTitle(mediaItem: Media) {
        title.text = mediaItem.title
    }

    fun updateMedia(mediaList: List<Media>) {
        if (this.mediaList.isNotEmpty()) this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
    }

    class MediaHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: AppCompatTextView = itemView.title
        val avatar: AppCompatImageView = itemView.avatar
    }
}
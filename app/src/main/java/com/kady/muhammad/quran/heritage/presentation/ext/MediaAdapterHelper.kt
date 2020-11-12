package com.kady.muhammad.quran.heritage.presentation.ext

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.presentation.widget.HorizontalSwipeLayout
import com.kady.muhammad.quran.heritage.presentation.widget.HorizontalSwipeLayout.Companion.SWIPED_FRACTION

class MediaAdapterHelper(val context: Context, val mediaList: MutableList<Media>) {

    private val swipedMap: MutableMap<Int, String> = mutableMapOf()

    private fun setSwipe(horizontalSwipeLayout: HorizontalSwipeLayout, isSwiped: Boolean) {
        with(horizontalSwipeLayout) { if (isSwiped) toMaxSwipe(false) else swipeBack(false) }
    }

    fun restoreHorizontalSwipeLayoutState(
        adapterPosition: Int,
        horizontalSwipeLayout: HorizontalSwipeLayout
    ) {
        val isSwiped: String? = swipedMap[adapterPosition]
        setSwipe(horizontalSwipeLayout, isSwiped = isSwiped != null)
    }

    fun onHorizontalSwipe(adapterPosition: Int, fraction: Float) {
        if (adapterPosition == RecyclerView.NO_POSITION) return
        if (fraction == SWIPED_FRACTION) {
            val mediaId: String = mediaList[adapterPosition].id
            swipedMap[adapterPosition] = mediaId
        } else if (fraction < SWIPED_FRACTION) {
            swipedMap.remove(adapterPosition)
        }
    }
}
package com.kady.muhammad.quran.heritage.presentation.widget

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.databinding.MenuItemBinding
import com.kady.muhammad.quran.heritage.databinding.OptionMenuBinding
import com.kady.muhammad.quran.heritage.presentation.common.animateHeight
import com.kady.muhammad.quran.heritage.presentation.common.px
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity
import kotlin.math.ceil

class OptionMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val statusBarHeight: Float = ceil((25 * context.resources.displayMetrics.density))
    private val adapter = ItemsAdapter()
    private val content: FrameLayout by lazy { (context as Activity).findViewById(android.R.id.content) }
    private val colorViewModel by lazy { (context as MainActivity).colorViewModel }
    private val binding: OptionMenuBinding
    private val menuItems: MutableList<MenuItem> = mutableListOf()

    //
    private var isPopupShown = false
    private var onItemClickListener: ((Int) -> Unit)? = null

    init {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.option_menu, this, true)
        binding.colorViewModel = colorViewModel
        isFocusable = true
        isFocusableInTouchMode = true
        //
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        //
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                val recyclerViewRect = Rect()
                binding.cardView.getGlobalVisibleRect(recyclerViewRect)
                if (!recyclerViewRect.contains(x, y)) {
                    hide()
                    return@setOnTouchListener true
                }
            }
            if (event.action == MotionEvent.ACTION_UP) {
                performClick()
                return@setOnTouchListener true
            }
            false
        }
        visibility = View.INVISIBLE
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK && isPopupShown) {
            hide()
            return true
        }
        return super.onKeyPreIme(keyCode, event)
    }

    fun addMenuItems(menuItems: List<MenuItem>) {
        this.menuItems.clear()
        this.menuItems.addAll(menuItems)
    }

    fun show(anchorView: View) {
        isPopupShown = true
        //
        post {
            binding.cardView.x = anchorView.x + 8F.px
            binding.cardView.y = anchorView.y + statusBarHeight + 8F.px
            visibility = View.VISIBLE
        }
        content.addView(this)
        binding.cardView.animateHeight(duration = 350)
        postDelayed({
            requestFocus()
        }, 100)
        adapter.updateAdapter(menuItems)
    }

    fun addOnItemClickListener(onItemClickListener: (Int) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    private fun hide() {
        isPopupShown = false
        binding.cardView.animateHeight(duration = 350, true) {
            content.removeView(this)
        }
    }

    data class MenuItem(val title: String, val icon: Int)

    private inner class ItemsAdapter(val items: MutableList<MenuItem> = mutableListOf()) :
        RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val li = LayoutInflater.from(context)
            return ViewHolder(DataBindingUtil.inflate(li, R.layout.menu_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder.itemView).apply {
                holder.binding.titleTextView.text = items[position].title
                holder.binding.iconImageView.setImageResource(items[position].icon)
                setOnClickListener {
                    hide()
                    onItemClickListener?.invoke(position)
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun updateAdapter(menuItems: List<MenuItem>) {
            this.items.clear()
            this.items.addAll(menuItems)
            notifyItemRangeInserted(0, menuItems.size)
        }

        inner class ViewHolder(val binding: MenuItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.colorViewModel = colorViewModel
            }
        }

    }

}
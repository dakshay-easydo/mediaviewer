package com.liberty.mediaviewer.common.extensions

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.configureForMediaViewer(
    isIdle: (Boolean) -> Unit,
    onPageChange: (Int) -> Unit
) {
    fun View.findFirstRecyclerView(): RecyclerView? {
        if (this is RecyclerView) return this
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i).findFirstRecyclerView()
                if (child != null) return child
            }
        }
        return null
    }

    val recyclerView = findFirstRecyclerView() ?: return
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            isIdle(state == ViewPager2.SCROLL_STATE_IDLE)
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onPageChange(position)
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    recyclerView.setOnTouchListener { v, ev ->
        if (ev.pointerCount > 1) {
            v.parent?.requestDisallowInterceptTouchEvent(true)
            return@setOnTouchListener false
        }
        false
    }
}


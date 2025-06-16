package com.liberty.mediaviewer.viewer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liberty.mediaviewer.viewer.fragment.ImageFragment
import com.liberty.mediaviewer.viewer.fragment.MediaAwareImpl
import com.liberty.mediaviewer.viewer.fragment.VideoFragment
import java.lang.ref.WeakReference

class MediaViewPager2Adapter<T>(
    activity: FragmentActivity,
    private var items: List<T>,
    private val isVideo: (T) -> Boolean,
    private val getMediaPath: (T) -> String,
    private val zoomEnable: Boolean = true
) : FragmentStateAdapter(activity) {
    private val scaleAwareImpls = mutableMapOf<Int, WeakReference<MediaAwareImpl>>()

    internal fun getItem(position: Int): T {
        return items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return items.any { it.hashCode().toLong() == itemId }
    }

    internal fun isScaled(position: Int): Boolean {
        val fragment = scaleAwareImpls[position]?.get() ?: return false
        return fragment.isScaled()
    }

    internal fun resetScale(position: Int, animate: Boolean = true) {
        val fragment = scaleAwareImpls[position]?.get() ?: return
        fragment.resetScale(animate)
    }

    internal fun scale(position: Int, enable: Boolean) {
        val fragment = scaleAwareImpls[position]?.get() ?: return
        fragment.scale(enable)
    }

    internal fun stopPlayback(position: Int) {
        val fragment = scaleAwareImpls[position]?.get() ?: return
        fragment.stopPlayback()
    }

    internal fun release() {
        scaleAwareImpls.values.forEach {
            it.get()?.release()
        }
        scaleAwareImpls.clear()
    }

    override fun createFragment(position: Int): Fragment {
        val item = getItem(position)
        val path = getMediaPath.invoke(item)
        val fragment = when {
            isVideo.invoke(item) -> VideoFragment.newInstance(
                path = path,
                zoomEnable = zoomEnable
            )

            else -> ImageFragment.newInstance(
                path = path,
                zoomEnable = zoomEnable
            )
        }
        scaleAwareImpls.put(position, WeakReference(fragment))
        return fragment
    }

    internal fun updateMedias(newList: List<T>) {
        val callback = DiffCallback(items, newList)
        val diffResult = DiffUtil.calculateDiff(callback)
        items = newList.toList()
        diffResult.dispatchUpdatesTo(this)
    }

    internal class DiffCallback<T>(
        private val old: List<T>,
        private val new: List<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            old[oldPos] == new[newPos]

        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            old[oldPos] == new[newPos]
    }
}
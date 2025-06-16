package com.liberty.mediaviewer.common.pager

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.liberty.mediaviewer.common.extensions.forEach
import java.lang.ref.WeakReference

internal abstract class RecyclingPagerAdapter<VH : RecyclingPagerAdapter.ViewHolder>
    : PagerAdapter() {

    private val typeCaches = SparseArray<RecycleCache>()
    private var savedStates = SparseArray<Parcelable>()

    internal abstract fun getItemCount(): Int
    internal abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    internal abstract fun onBindViewHolder(holder: VH, position: Int)
    open fun getItemId(position: Int): Int = position

    override fun getCount() = getItemCount()
    override fun getItemPosition(item: Any): Int = POSITION_NONE

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val viewType = 0
        val cache = typeCaches[viewType] ?: RecycleCache(this).also {
            typeCaches.put(viewType, it)
        }
        @Suppress("UNCHECKED_CAST")
        return cache.getFreeViewHolder(parent, viewType).apply {
            attach(parent, position)
            onBindViewHolder(this as VH, position)
            onRestoreInstanceState(savedStates.get(getItemId(position)))
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean =
        obj is ViewHolder && obj.itemView === view

    override fun saveState(): Parcelable? {
        for (viewHolder in getAttachedViewHolders()) {
            savedStates.put(getItemId(viewHolder.position), viewHolder.onSaveInstanceState())
        }
        return Bundle().apply { putSparseParcelableArray("STATE", savedStates) }
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        if (state is Bundle) {
            state.classLoader = loader
            savedStates = state.getSparseParcelableArray("STATE") ?: SparseArray()
        }
    }

    private fun getAttachedViewHolders(): List<ViewHolder> {
        return buildList {
            for (i in 0 until typeCaches.size()) {
                val cache = typeCaches.valueAt(i)
                addAll(cache.caches.filter { it.isAttached })
            }
        }
    }

    private class RecycleCache(
        private val adapter: RecyclingPagerAdapter<*>,
        private val maxSize: Int = 6
    ) {
        internal val caches = ArrayDeque<ViewHolder>()

        fun getFreeViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val existing = caches.firstOrNull { !it.isAttached }
            if (existing != null) return existing

            return if (caches.size < maxSize) {
                adapter.onCreateViewHolder(parent, viewType).also { caches.add(it) }
            } else {
                // Reuse LRU
                caches.removeFirst().also { caches.add(it) }
            }
        }
    }

    internal abstract class ViewHolder(internal val itemView: View) {
        internal var position: Int = 0
        internal var isAttached: Boolean = false

        internal fun attach(parent: ViewGroup, position: Int) {
            this.position = position
            isAttached = true
            parent.addView(itemView)
        }

        internal fun detach(parent: ViewGroup) {
            parent.removeView(itemView)
            isAttached = false
        }

        internal fun onSaveInstanceState(): Parcelable {
            val state = SparseArray<Parcelable>()
            itemView.saveHierarchyState(state)
            return Bundle().apply { putSparseParcelableArray("STATE", state) }
        }

        internal fun onRestoreInstanceState(state: Parcelable?) {
            (state as? Bundle)?.getSparseParcelableArray<Parcelable>("STATE")?.let {
                itemView.restoreHierarchyState(it)
            }
        }
    }
}

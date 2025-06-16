package com.liberty.mediaviewer.viewer.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.liberty.mediaviewer.loader.ImageLoader

class MediaPagerAdapter2<T>(
    private val context: Context,
    private var items: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val isVideo: ((T) -> Boolean)?,
    private val getMediaPath: ((T) -> String)?
) : ListAdapter<Any, RecyclerView.ViewHolder>(Diff()) {

    init {
        submitList(items)
    }

    class Diff<T : Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }


    companion object {
        private const val TYPE_IMAGE = 1
        private const val TYPE_VIDEO = 2
    }


    private var currentVideoPlayer: ExoPlayer? = null
    private var currentPlayerView: PlayerView? = null

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (isVideo?.invoke(items[position]) == true) TYPE_VIDEO else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val photoView = PhotoView(context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
            ImageViewHolder(photoView)
        } else {
            val playerView = PlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                useController = true
            }
            VideoViewHolder(playerView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ImageViewHolder -> imageLoader.loadImage(holder.photoView, item)
            is VideoViewHolder -> {
                val mediaPath = getMediaPath?.invoke(item) ?: return
                val player = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(mediaPath))
                    prepare()
                    playWhenReady = false
                }
                holder.playerView.player = player
                holder.playerView.useController = true
                if (currentVideoPlayer == null) {
                    currentVideoPlayer = player
                    currentPlayerView = holder.playerView
                }
            }
        }
    }

    fun handlePageSelected(position: Int) {
        val item = items.getOrNull(position) ?: return
        if (isVideo?.invoke(item) == true) {
            val holder = getViewHolderAt(position)
            if (holder is VideoViewHolder) {
                currentVideoPlayer?.pause()
                val path = getMediaPath?.invoke(item) ?: return
                val player = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(path))
                    prepare()
                    playWhenReady = true
                }
                holder.playerView.player = player
                currentVideoPlayer = player
                currentPlayerView = holder.playerView
            }
        } else {
            currentVideoPlayer?.pause()
        }
    }

    fun updateMedias(newItems: List<T>) {
        this.items = newItems
        submitList(newItems)
        notifyDataSetChanged()
    }

    private fun getViewHolderAt(position: Int): RecyclerView.ViewHolder? {
        return null
//        return try {
//            (context as? Activity)
//                ?.findViewById<RecyclerView>(R.id.viewPager)
//                ?.findViewHolderForAdapterPosition(position)
//        } catch (e: Exception) {
//            null
//        }
    }


    class ImageViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)

    class VideoViewHolder(val playerView: PlayerView) : RecyclerView.ViewHolder(playerView)


}

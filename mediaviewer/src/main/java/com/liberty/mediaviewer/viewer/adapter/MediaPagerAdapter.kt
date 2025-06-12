/*
 * Copyright 2018 Liberty Infospace
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liberty.mediaviewer.viewer.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.ui.PlayerView
import com.github.chrisbanes.photoview.PhotoView
import com.liberty.mediaviewer.common.extensions.resetScale
import com.liberty.mediaviewer.common.pager.RecyclingPagerAdapter
import com.liberty.mediaviewer.loader.ImageLoader
import java.io.File

internal class MediaPagerAdapter<T>(
    private val context: Context,
    private var items: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val isZoomingAllowed: Boolean = true,
    private val isVideo: ((T) -> Boolean)? = null,
    private val getMediaPath: ((T) -> String)? = null,
) : RecyclingPagerAdapter<MediaPagerAdapter<T>.ViewHolder>() {

    private val viewLayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    private val holders = mutableListOf<ViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FrameLayout(context).apply {
            layoutParams = viewLayoutParams
        }).also { holders.add(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = items.size

    fun updateMedias(newItems: List<T>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun isScaled(position: Int): Boolean =
        holders.firstOrNull {
            it.position == position
        }?.isScaled == true

    fun resetScale(position: Int) {
        holders.firstOrNull { it.position == position }?.resetScale()
    }

    internal inner class ViewHolder(
        itemView: View
    ) : RecyclingPagerAdapter.ViewHolder(itemView) {

        private val container = itemView as FrameLayout
        private var photoView: PhotoView? = null
        private var playerView: PlayerView? = null
        private var exoPlayer: ExoPlayer? = null

        var isScaled: Boolean = false
            get() = (photoView?.scale ?: 1f) > 1f


        fun bind(position: Int) {
            this.position = position
            val item = items[position]
            container.removeAllViews()

            if (isVideo?.invoke(item) == true) {
                val path = getMediaPath?.invoke(item)
                if (path == null) setupPhotoView(item)
                else setupPlayerView(path)
            } else {
                setupPhotoView(item)
            }
        }

        fun resetScale() {
            photoView?.resetScale(animate = true)
        }

        private fun setupPhotoView(item: T) {
            photoView = PhotoView(context).apply {
                isEnabled = isZoomingAllowed
                setAllowParentInterceptOnEdge(true)
                layoutParams = viewLayoutParams
            }
            container.addView(photoView)
            imageLoader.loadImage(photoView!!, item)
        }

        private fun setupPlayerView(path: String) {
            val uri = if (path.startsWith("http")) Uri.parse(path) else Uri.fromFile(File(path))

            playerView = PlayerView(context).apply {
                layoutParams = viewLayoutParams
                useController = true
            }

            exoPlayer = Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
            playerView?.player = exoPlayer
            container.addView(playerView)
        }

        fun releasePlayer() {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    fun releaseAllPlayers() {
        holders.forEach { it.releasePlayer() }
    }

}
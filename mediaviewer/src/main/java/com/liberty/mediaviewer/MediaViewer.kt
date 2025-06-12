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
package com.liberty.mediaviewer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.liberty.mediaviewer.listeners.OnDismissListener
import com.liberty.mediaviewer.listeners.OnMediaChangeListener
import com.liberty.mediaviewer.loader.ImageLoader
import com.liberty.mediaviewer.viewer.builder.BuilderData
import com.liberty.mediaviewer.viewer.dialog.MediaViewerDialog
import kotlin.math.roundToInt

open class MediaViewer<T> protected constructor(
    val context: Context,
    val builderData: BuilderData<T>
) {
    private val dialog = MediaViewerDialog<T>(context, builderData)


    /**
     * Displays the built viewer if passed list of images is not empty
     *
     * @param animate whether the passed transition view should be animated on open. Useful for screen rotation handling.
     */
    /**
     * Displays the built viewer if passed list of images is not empty
     */
    @JvmOverloads
    fun show(animate: Boolean = true) {
        if (!builderData.medias.isEmpty()) {
            dialog.show(animate)
        } else {
            Log.w(
                context.getString(R.string.library_name),
                "Images list cannot be empty! Viewer ignored."
            )
        }
    }

    /**
     * Closes the viewer with suitable close animation
     */
    fun close() {
        dialog.close()
    }

    /**
     * Dismisses the dialog with no animation
     */
    fun dismiss() {
        dialog.dismiss()
    }

    /**
     * Updates an existing images list if a new list is not empty, otherwise closes the viewer
     */
    fun updateMedias(images: Array<T>) {
        updateMedias(images.toList())
    }

    /**
     * Updates an existing images list if a new list is not empty, otherwise closes the viewer
     */
    fun updateMedias(medias: List<T>) {
        if (!medias.isEmpty()) {
            dialog.updateMedias(medias)
        } else {
            dialog.close()
        }
    }

    fun currentPosition(): Int {
        return dialog.getCurrentPosition()
    }

    fun setCurrentPosition(position: Int): Int {
        return dialog.setCurrentPosition(position)
    }

    /**
     * Updates transition image view.
     * Useful for a case when image position has changed and you want to update the transition animation target.
     */
    fun updateTransitionImage(imageView: ImageView?) {
        dialog.updateTransitionImage(imageView)
    }

    /**
     * Builder class for [MediaViewer]
     */
    class Builder<T>(
        val context: Context,
        val medias: List<T>,
        val imageLoader: ImageLoader<T>,
        val isVideo: ((T) -> Boolean)? = null,
        val getMediaPath: ((T) -> String)? = null,
    ) {
        private val data: BuilderData<T> = BuilderData<T>(
            medias = medias,
            imageLoader = imageLoader,
            isVideo = isVideo,
            getMediaPath = getMediaPath
        )


        /**
         * Sets a position to start viewer from.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withStartPosition(position: Int): Builder<T> {
            this.data.startPosition = position
            return this
        }

        /**
         * Sets a background color value for the viewer
         *
         * @return This Builder object to allow calls chaining
         */
        fun withBackgroundColor(@ColorInt color: Int): Builder<T> {
            this.data.backgroundColor = color
            return this
        }

        /**
         * Sets a background color resource for the viewer
         *
         * @return This Builder object to allow calls chaining
         */
        fun withBackgroundColorResource(@ColorRes color: Int): Builder<T> {
            return this.withBackgroundColor(ContextCompat.getColor(context, color))
        }

        /**
         * Sets custom overlay view to be shown over the viewer.
         * Commonly used for image description or counter displaying.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withOverlayView(view: View?): Builder<T> {
            this.data.overlayView = view
            return this
        }

        /**
         * Sets space between the images using dimension.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withImagesMargin(@DimenRes dimen: Int): Builder<T> {
            this.data.imageMarginPixels = context.resources.getDimension(dimen).roundToInt()
            return this
        }

        /**
         * Sets space between the images in pixels.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withImageMarginPixels(marginPixels: Int): Builder<T> {
            this.data.imageMarginPixels = marginPixels
            return this
        }

        /**
         * Sets overall padding for zooming and scrolling area using dimension.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withContainerPadding(@DimenRes padding: Int): Builder<T> {
            val paddingPx = context.resources.getDimension(padding).roundToInt()
            return withContainerPaddingPixels(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        /**
         * Sets `start`, `top`, `end` and `bottom` padding for zooming and scrolling area using dimension.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withContainerPadding(
            @DimenRes start: Int, @DimenRes top: Int,
            @DimenRes end: Int, @DimenRes bottom: Int
        ): Builder<T> {
            val resources = context.resources
            withContainerPaddingPixels(
                resources.getDimension(start).roundToInt(),
                resources.getDimension(top).roundToInt(),
                resources.getDimension(end).roundToInt(),
                resources.getDimension(bottom).roundToInt()
            )
            return this
        }

        /**
         * Sets overall padding for zooming and scrolling area in pixels.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withContainerPaddingPixels(@Px padding: Int): Builder<T> {
            this.data.containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
            return this
        }

        /**
         * Sets `start`, `top`, `end` and `bottom` padding for zooming and scrolling area in pixels.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withContainerPaddingPixels(start: Int, top: Int, end: Int, bottom: Int): Builder<T> {
            this.data.containerPaddingPixels = intArrayOf(start, top, end, bottom)
            return this
        }

        /**
         * Sets status bar visibility. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withHiddenStatusBar(value: Boolean): Builder<T> {
            this.data.shouldStatusBarHide = value
            return this
        }

        /**
         * Enables or disables zooming. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        fun allowZooming(value: Boolean): Builder<T> {
            this.data.isZoomingAllowed = value
            return this
        }

        /**
         * Enables or disables the "Swipe to Dismiss" gesture. True by default.
         *
         * @return This Builder object to allow calls chaining
         */
        fun allowSwipeToDismiss(value: Boolean): Builder<T> {
            this.data.isSwipeToDismissAllowed = value
            return this
        }

        /**
         * Sets a target [ImageView] to be part of transition when opening or closing the viewer/
         *
         * @return This Builder object to allow calls chaining
         */
        fun withTransitionFrom(imageView: ImageView?): Builder<T> {
            this.data.transitionView = imageView
            return this
        }

        /**
         * Sets [OnMediaChangeListener] for the viewer.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withImageChangeListener(imageChangeListener: OnMediaChangeListener?): Builder<T> {
            this.data.mediaChangeListener = imageChangeListener
            return this
        }

        /**
         * Sets [OnDismissListener] for viewer.
         *
         * @return This Builder object to allow calls chaining
         */
        fun withDismissListener(onDismissListener: OnDismissListener?): Builder<T> {
            this.data.onDismissListener = onDismissListener
            return this
        }

        /**
         * Creates a [MediaViewer] with the arguments supplied to this builder. It does not
         * show the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use [.show] if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        fun build(): MediaViewer<T> {
            return MediaViewer<T>(context, data)
        }

        /**
         * Creates the [MediaViewer] with the arguments supplied to this builder and
         * shows the dialog.
         *
         * @param animate whether the passed transition view should be animated on open. Useful for screen rotation handling.
         */
        /**
         * Creates the [MediaViewer] with the arguments supplied to this builder and
         * shows the dialog.
         */
        @JvmOverloads
        fun show(animate: Boolean = true): MediaViewer<T> {
            val viewer = build()
            viewer.show(animate)
            return viewer
        }
    }
}

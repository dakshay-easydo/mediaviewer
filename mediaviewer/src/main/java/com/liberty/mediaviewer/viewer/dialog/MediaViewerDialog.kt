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

package com.liberty.mediaviewer.viewer.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.liberty.mediaviewer.R
import com.liberty.mediaviewer.common.extensions.findFragmentActivity
import com.liberty.mediaviewer.viewer.builder.BuilderData
import com.liberty.mediaviewer.viewer.view.MediaViewerView

internal class MediaViewerDialog<T>(
    context: Context,
    private val builderData: BuilderData<T>
) {
    private val dialog: AlertDialog
    private val viewerView: MediaViewerView<T> = MediaViewerView(context)
    private var animateOpen = true

    private val dialogStyle: Int
        get() = if (builderData.shouldStatusBarHide)
            R.style.ImageViewerDialog_NoStatusBar
        else
            R.style.ImageViewerDialog_Default

    init {
        setupViewerView()
        addLifecycle(context)
        dialog = AlertDialog
            .Builder(context, dialogStyle)
            .setView(viewerView)
            .setOnKeyListener { _, keyCode, event -> onDialogKeyEvent(keyCode, event) }
            .create()
            .apply {
                setOnShowListener {
                    viewerView.open(
                        transitionImageView = builderData.transitionView,
                        animate = animateOpen
                    )
                }
                setOnDismissListener {
                    viewerView.release()
                    builderData.onDismissListener?.onDismiss()
                }
            }
    }

    private fun addLifecycle(context: Context) {
        context.findFragmentActivity()?.lifecycle?.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    dialog.dismiss()
                }
            })
    }

    fun show(animate: Boolean) {
        animateOpen = animate

        val window = dialog.window ?: return

        // Make dialog temporarily not focusable to allow applying system UI changes
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        // Allow edge-to-edge drawing
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        dialog.show()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Apply immersive system UI visibility
        if (builderData.shouldStatusBarHide) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        // Clear the FLAG_NOT_FOCUSABLE so dialog can receive input
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        // Ensure dialog extends into the cutout area if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val layoutParams = window.attributes
            layoutParams.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            window.attributes = layoutParams
        }

        // Optional: on older APIs where edge-to-edge causes issues, show status bar
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            insetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    fun close() {
        viewerView.close()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun updateMedias(medias: List<T>) {
        viewerView.updateMedias(medias)
    }

    fun getCurrentPosition(): Int =
        viewerView.currentPosition

    fun setCurrentPosition(position: Int): Int {
        viewerView.currentPosition = position
        return viewerView.currentPosition
    }

    fun updateTransitionImage(imageView: ImageView?) {
        viewerView.updateTransitionImage(imageView)
    }

    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP &&
            !event.isCanceled
        ) {
            if (viewerView.isScaled) {
                viewerView.resetScale()
            } else {
                viewerView.close()
            }
            return true
        }
        return false
    }

    private fun setupViewerView() {
        viewerView.apply {
            isZoomingAllowed = builderData.isZoomingAllowed
            isSwipeToDismissAllowed = builderData.isSwipeToDismissAllowed
            containerPadding = builderData.containerPaddingPixels
            imagesMargin = builderData.imageMarginPixels
            overlayView = builderData.overlayView
            backgroundColor = builderData.backgroundColor
            onPageChange = { builderData.mediaChangeListener?.onMediaChange(it) }
            onDismiss = { dialog.dismiss() }
            setItems(
                images = builderData.medias,
                startPosition = builderData.startPosition,
                isVideo = builderData.isVideo,
                getMediaPath = builderData.getMediaPath,
            )
        }
    }
}
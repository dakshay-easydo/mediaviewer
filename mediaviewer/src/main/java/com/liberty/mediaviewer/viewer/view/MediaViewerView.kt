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

package com.liberty.mediaviewer.viewer.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager2.widget.ViewPager2
import com.liberty.mediaviewer.R
import com.liberty.mediaviewer.common.decorators.HorizontalMarginDecoration
import com.liberty.mediaviewer.common.extensions.animateAlpha
import com.liberty.mediaviewer.common.extensions.applyMargin
import com.liberty.mediaviewer.common.extensions.copyBitmapFrom
import com.liberty.mediaviewer.common.extensions.findFragmentActivity
import com.liberty.mediaviewer.common.extensions.isRectVisible
import com.liberty.mediaviewer.common.extensions.isVisible
import com.liberty.mediaviewer.common.extensions.makeGone
import com.liberty.mediaviewer.common.extensions.makeInvisible
import com.liberty.mediaviewer.common.extensions.makeVisible
import com.liberty.mediaviewer.common.extensions.switchVisibilityWithAnimation
import com.liberty.mediaviewer.common.gestures.detector.SimpleOnGestureListener
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirection
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirection.DOWN
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirection.LEFT
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirection.RIGHT
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirection.UP
import com.liberty.mediaviewer.common.gestures.direction.SwipeDirectionDetector
import com.liberty.mediaviewer.common.gestures.dismiss.SwipeToDismissHandler
import com.liberty.mediaviewer.common.extensions.configureForMediaViewer
import com.liberty.mediaviewer.common.tranformer.DepthPageTransformer
import com.liberty.mediaviewer.viewer.adapter.MediaViewPager2Adapter
import kotlin.math.abs


internal class MediaViewerView<T>
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    internal var isZoomingAllowed = true
    internal var isSwipeToDismissAllowed = true

    internal var currentPosition: Int
        get() = viewPager.currentItem
        set(value) {
            viewPager.currentItem = value
        }

    internal var onDismiss: (() -> Unit)? = null
    internal var onPageChange: ((position: Int, t: T?) -> Unit)? = null

    internal val isScaled
        get() = mediaAdapter?.isScaled(currentPosition) == true

    internal var containerPadding = intArrayOf(0, 0, 0, 0)


    private var marginDecoration = HorizontalMarginDecoration()

    internal var imagesMargin
        get() = marginDecoration.marginPx
        set(value) {
            viewPager.removeItemDecoration(marginDecoration)
            marginDecoration = HorizontalMarginDecoration(value)
            viewPager.addItemDecoration(marginDecoration)
        }

    internal var isPagerIdle = true
    internal var overlayView: View? = null
        set(value) {
            field = value
            value?.let { rootContainer.addView(it) }
        }


    internal var backgroundColor: Int = Color.BLACK
        set(value) {
            field = value
            backgroundView.setBackgroundColor(value)
        }

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private val transitionImageView: ImageView
    private var externalTransitionImageView: ImageView? = null

    private var viewPager: ViewPager2
    private var mediaAdapter: MediaViewPager2Adapter<T>? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetector
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null

    private var items: List<T> = listOf()
    private var isVideo: ((T) -> Boolean)? = null
    private var getMediaPath: ((T) -> String)? = null
    private lateinit var transitionImageAnimator: TransitionImageAnimator

    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
                || !externalTransitionImageView.isRectVisible
                || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition


    init {
        inflate(context, R.layout.view_image_viewer, this)

        rootContainer = findViewById(R.id.rootContainer)
        checkNotNull(rootContainer) { "rootContainer is null!" }

        backgroundView = findViewById(R.id.backgroundView)

        checkNotNull(backgroundView) { "backgroundView is null!" }

        dismissContainer = findViewById(R.id.dismissContainer)
        checkNotNull(dismissContainer) { "dismissContainer is null!" }


        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        checkNotNull(transitionImageContainer) { "transitionImageContainer is null!" }

        transitionImageView = findViewById(R.id.transitionImageView)
        checkNotNull(transitionImageView) { "transitionImageView is null!" }

        viewPager = findViewById(R.id.viewPager)

        checkNotNull(viewPager) { "viewPager is null!" }

        viewPager.setPageTransformer(DepthPageTransformer())
        viewPager.configureForMediaViewer(
            isIdle = { isPagerIdle = it },
            onPageChange = {
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) makeInvisible() else makeVisible()
                }
                onPageChange?.invoke(it, mediaAdapter?.getItem(it))
            })


        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true) {
            return true
        }

        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1
        ) {
            return true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            return viewPager.dispatchTouchEvent(event)
        }

        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
    }

    override fun setBackgroundColor(color: Int) {
        backgroundView.setBackgroundColor(color)
    }

    internal fun setItems(
        images: List<T>,
        startPosition: Int,
        isVideo: (T) -> Boolean,
        getMediaPath: (T) -> String,
    ) {
        this.items = images
        this.isVideo = isVideo
        this.getMediaPath = getMediaPath
        this.startPosition = startPosition
        this.mediaAdapter = MediaViewPager2Adapter(
            activity = context.findFragmentActivity()!!,
            items = images,
            zoomEnable = isZoomingAllowed,
            isVideo = isVideo,
            getMediaPath = getMediaPath,
        )

        this.viewPager.adapter = mediaAdapter
        currentPosition = startPosition
    }

    internal fun open(transitionImageView: ImageView?, animate: Boolean) {
        prepareViewsForTransition()
        externalTransitionImageView = transitionImageView
        this.transitionImageView.copyBitmapFrom(transitionImageView)

        transitionImageAnimator = createTransitionImageAnimator(transitionImageView)
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)

        if (animate) animateOpen() else prepareViewsForViewer()
    }

    internal fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
        } else {
            animateClose()
        }
    }

    internal fun updateMedias(images: List<T>) {
        val old = items[currentPosition]
        val newPosition = images.indexOfFirst { it == old }
        this.items = images
        mediaAdapter?.updateMedias(images)

        val currentPosition = viewPager.currentItem.coerceAtMost(images.lastIndex)
        viewPager.setCurrentItem(currentPosition, false)

        if (currentPosition != newPosition && newPosition != -1) {
            viewPager.setCurrentItem(newPosition, false)
        }
    }

    internal fun updateTransitionImage(imageView: ImageView?) {
        externalTransitionImageView?.makeVisible()
        imageView?.makeInvisible()

        externalTransitionImageView = imageView
        startPosition = currentPosition
        transitionImageAnimator = createTransitionImageAnimator(imageView)
//        imageLoader?.loadImage(transitionImageView, items[startPosition])
    }

    internal fun resetScale() {
        mediaAdapter?.resetScale(currentPosition)
    }

    internal fun scale(enable: Boolean) {
        mediaAdapter?.scale(currentPosition, enable)
    }

    internal fun release() {
        mediaAdapter?.release()
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            containerPadding = containerPadding,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView?.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = { prepareViewsForViewer() })
    }

    private fun animateClose() {
        release()
        prepareViewsForTransition()
        dismissContainer.applyMargin(0, 0, 0, 0)
        transitionImageAnimator.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
            },
            onTransitionEnd = { onDismiss?.invoke() })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.makeVisible()
        viewPager.makeGone()
    }

    private fun prepareViewsForViewer() {
        backgroundView.alpha = 1f
        transitionImageContainer.makeGone()
        viewPager.makeVisible()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            UP, DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && isPagerIdle) {
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }

            LEFT, RIGHT -> {
                viewPager.dispatchTouchEvent(event)
            }

            else -> true
        }
    }

    private fun handleUpDownEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    private fun handleEventActionDown(event: MotionEvent) {
        swipeDirection = null
        wasScaled = false
        viewPager.dispatchTouchEvent(event)

        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        viewPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSingleTap(event: MotionEvent, isOverlayWasClicked: Boolean) {
        if (overlayView != null && !isOverlayWasClicked) {
            overlayView?.switchVisibilityWithAnimation()
            super.dispatchTouchEvent(event)
        }
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView?.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            ?.let { it.isVisible && it.dispatchTouchEvent(event) } == true

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetector(context, SimpleOnGestureListener(
            onSingleTap = {
                if (isPagerIdle) {
                    handleSingleTap(it, isOverlayWasClicked)
                }
                false
            },
            onDoubleTap = {
                wasDoubleTapped = !isScaled
                false
            }
        ))

    private fun createScaleGestureDetector() =
        ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())

    private fun createSwipeToDismissHandler()
            : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = dismissContainer,
        shouldAnimateDismiss = { shouldDismissToBottom },
        onDismiss = { animateClose() },
        onSwipeViewMove = ::handleSwipeViewMove
    )

    private fun createTransitionImageAnimator(transitionImageView: ImageView?) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = this.transitionImageView,
            internalImageContainer = this.transitionImageContainer
        )


}
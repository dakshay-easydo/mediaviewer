package com.liberty.mediaviewer.viewer.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.*
import com.liberty.mediaviewer.R

class MultiTouchPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), CoroutineScope by MainScope() {

    private val playerView = PlayerView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        useController = false
        @UnstableApi setShutterBackgroundColor(Color.TRANSPARENT)
        setOnTouchListener(::onTouchUpdated)
    }

    private val controlOverlay = FrameLayout(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setOnClickListener { toggleControls() }
        isVisible = false
    }

    private val playPauseButton = ImageView(context).apply {
        layoutParams = LayoutParams(64.dp, 64.dp).apply {
            gravity = Gravity.CENTER
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        setImageResource(R.drawable.ic_play_24px)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#88000000")) // black with 53% alpha
        }
        setPadding(14.dp, 14.dp, 14.dp, 14.dp)

        setOnClickListener {
            togglePlayPause()
            showControlsTemporarily()
        }
    }

    private val seekBar = SafeSliderView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            setMargins(32, 0, 32, 32)
            visibility = GONE
        }
        addOnChangeListener { value, fromUser ->
            if (fromUser) {
                player?.seekTo((value * (player?.duration ?: 1)).toLong())
            }
        }
    }

    private var controlsVisible = false
    private var player: ExoPlayer? = null
    private var updateJob: Job? = null

    // Zoom/gesture variables
    private var scaleFactor = 1f
    private var translationX = 0f
    private var translationY = 0f
    var zoomEnable = false

    private val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (!zoomEnable) return false

                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(1f, 4f)

                playerView.pivotX = detector.focusX
                playerView.pivotY = detector.focusY
                applyTransform()
                return true
            }
        })

    private val gestureDetector = GestureDetector(
        context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleZoom()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                toggleControls()         // shows/hides overlay with animation
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!zoomEnable) return false
                if (scaleFactor > 1f) {
                    translationX -= distanceX
                    translationY -= distanceY
                    applyTransform()
                    return true
                }

                // Gesture seek only if not zoomed
                val diffX = e2.x - (e1?.x ?: 0f)
                if (kotlin.math.abs(diffX) > 50 && player != null && player!!.isPlaying) {
                    val seekOffset = (player!!.duration * (diffX / width.toFloat())) // Proportional
                    player!!.seekTo(
                        (player!!.currentPosition + seekOffset.toLong()).coerceIn(
                            0,
                            player!!.duration
                        )
                    )
                    showControlsTemporarily()
                    return true
                }

                return false
            }
        })

    init {
        addView(playerView)
        controlOverlay.addView(playPauseButton)
        controlOverlay.addView(seekBar)
        addView(controlOverlay)
    }

    fun setPlayer(exoPlayer: ExoPlayer) {
        player = exoPlayer
        playerView.player = exoPlayer
        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        updateJob?.cancel()
        updateJob = launch {
            while (isActive) {
                val position = player?.currentPosition ?: 0
                val duration = player?.duration ?: 1
                seekBar.value = if (duration > 0) position.toFloat() / duration else 0f

                playPauseButton.setImageResource(
                    if (player?.isPlaying == true) R.drawable.ic_play_24px
                    else R.drawable.ic_pause_24px
                )
                delay(500)
            }
        }
    }


    private fun toggleControls() {
        if (controlsVisible) {
            hideControlsJob?.cancel()
            controlOverlay.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction {
                    controlOverlay.isVisible = false
                    controlOverlay.alpha = 1f
                    controlsVisible = false
                }
                .start()
        } else {
            showControlsTemporarily()
        }
    }

    private fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                playPauseButton.setImageResource(R.drawable.ic_play_24px)
            } else {
                it.play()
                playPauseButton.setImageResource(R.drawable.ic_pause_24px)
            }
        }
    }


    private var hideControlsJob: Job? = null
    private fun showControlsTemporarily() {
        controlOverlay.isVisible = true
        controlOverlay.alpha = 1f
        controlsVisible = true

        hideControlsJob?.cancel()
        hideControlsJob = launch {
            delay(3000)
            hideControls()
        }
    }

    private fun hideControls() {
        hideControlsJob?.cancel()
        controlOverlay.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction {
                controlOverlay.isVisible = false
                controlOverlay.alpha = 1f
                controlsVisible = false
            }
            .start()
    }

    private fun onTouchUpdated(v: View, event: MotionEvent): Boolean {
        if (!zoomEnable) return false

        if (event.pointerCount > 1) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        if (event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            fixTranslationBounds()
            if (!scaleDetector.isInProgress) v.performClick()
        }

        return true
    }

    private fun applyTransform() {
        val maxTransX = (playerView.width * (scaleFactor - 1)) / 2
        val maxTransY = (playerView.height * (scaleFactor - 1)) / 2

        translationX = translationX.coerceIn(-maxTransX, maxTransX)
        translationY = translationY.coerceIn(-maxTransY, maxTransY)

        playerView.scaleX = scaleFactor
        playerView.scaleY = scaleFactor
        playerView.translationX = translationX
        playerView.translationY = translationY
    }

    private fun fixTranslationBounds() {
        post { applyTransform() }
    }

    private fun toggleZoom() {
        if (!zoomEnable) return
        if (scaleFactor > 1f) {
            resetZoom(animated = true)
        } else {
            scaleFactor = 2f
            translationX = 0f
            translationY = 0f
            playerView.animate()
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .translationX(0f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    fun resetZoom(animated: Boolean = false) {
        if (!zoomEnable) return

        if (animated) {
            playerView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(250)
                .withEndAction {
                    scaleFactor = 1f
                    translationX = 0f
                    translationY = 0f
                }
                .start()
        } else {
            playerView.scaleX = 1f
            playerView.scaleY = 1f
            playerView.translationX = 0f
            playerView.translationY = 0f
            scaleFactor = 1f
            translationX = 0f
            translationY = 0f
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateJob?.cancel()
    }

    fun isZoomed(): Boolean = zoomEnable && scaleFactor != 1f

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}

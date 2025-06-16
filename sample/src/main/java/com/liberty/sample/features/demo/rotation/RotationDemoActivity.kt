package com.liberty.sample.features.demo.rotation

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.liberty.mediaviewer.MediaViewer
import com.liberty.sample.R
import com.liberty.sample.common.extensions.getDrawableCompat
import com.liberty.sample.common.extensions.loadImage
import com.liberty.sample.common.models.Demo
import com.liberty.sample.common.models.Poster
import com.liberty.sample.databinding.ActivityDemoRotationBinding

class RotationDemoActivity : AppCompatActivity() {

    companion object {
        private const val KEY_IS_DIALOG_SHOWN = "IS_DIALOG_SHOWN"
        private const val KEY_CURRENT_POSITION = "CURRENT_POSITION"
    }

    private var isDialogShown = false
    private var currentPosition: Int = 0

    private lateinit var viewer: MediaViewer<Poster>

    private lateinit var binding: ActivityDemoRotationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDemoRotationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rotationDemoImage.setOnClickListener { openViewer(0) }
        loadPosterImage(binding.rotationDemoImage, Demo.posters[0])
    }

    override fun onPause() {
        super.onPause()
        viewer.dismiss()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isDialogShown = savedInstanceState.getBoolean(KEY_IS_DIALOG_SHOWN)
        currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION)

        if (isDialogShown) {
            openViewer(currentPosition)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_DIALOG_SHOWN, isDialogShown)
        outState.putInt(KEY_CURRENT_POSITION, currentPosition)
        super.onSaveInstanceState(outState)
    }

    private fun openViewer(startPosition: Int) {
        viewer = MediaViewer.Builder<Poster>(
            context = this,
            medias = Demo.posters,
            getMediaPath = ::getMediaPath,
            isVideo = ::isVideo,
        )
            .withTransitionFrom(getTransitionTarget(startPosition))
            .withStartPosition(startPosition)
            .withMediaChangeListener {
                currentPosition = it
                viewer.updateTransitionImage(getTransitionTarget(it))
            }
            .withDismissListener { isDialogShown = false }
            .show(!isDialogShown)

        currentPosition = startPosition
        isDialogShown = true
    }

    private fun loadPosterImage(imageView: ImageView, poster: Poster?) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(poster?.url)
        }
    }

    private fun isVideo(poster: Poster): Boolean {
        return poster.url.endsWith(".mp4")
    }

    private fun getMediaPath(poster: Poster?): String {
        return poster?.url.orEmpty()
    }

    private fun getTransitionTarget(position: Int) =
        if (position == 0) binding.rotationDemoImage else null
}
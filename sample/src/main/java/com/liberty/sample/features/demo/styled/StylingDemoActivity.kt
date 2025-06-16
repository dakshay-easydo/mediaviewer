package com.liberty.sample.features.demo.styled

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.liberty.mediaviewer.MediaViewer
import com.liberty.sample.R
import com.liberty.sample.common.extensions.showShortToast
import com.liberty.sample.common.models.Demo
import com.liberty.sample.common.models.Poster
import com.liberty.sample.common.ui.base.BaseActivity
import com.liberty.sample.common.ui.views.PosterOverlayView
import com.liberty.sample.databinding.ActivityDemoStylingBinding
import com.liberty.sample.features.demo.styled.options.StylingOptions
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.CONTAINER_PADDING
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.HIDE_STATUS_BAR
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.IMAGES_MARGIN
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.RANDOM_BACKGROUND
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.SHOW_OVERLAY
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.SHOW_TRANSITION
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.SWIPE_TO_DISMISS
import com.liberty.sample.features.demo.styled.options.StylingOptions.Property.ZOOMING
import java.util.Random

class StylingDemoActivity : BaseActivity() {
    private lateinit var binding: ActivityDemoStylingBinding
    private var options = StylingOptions()
    private var overlayView: PosterOverlayView? = null
    private var viewer: MediaViewer<Poster>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDemoStylingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stylingPostersGridView.apply {
            imageLoader = ::loadPosterImage
            onPosterClick = ::openViewer
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.styling_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        options.showDialog(this)
        return super.onOptionsItemSelected(item)
    }

    private fun openViewer(startPosition: Int, imageView: ImageView) {
        val posters = Demo.posters.toMutableList()

        val builder = MediaViewer.Builder<Poster>(
            context = this,
            medias = Demo.posters,
            getMediaPath = ::getMediaPath,
            isVideo = ::isVideo,
        )
            .withStartPosition(startPosition)
            .withMediaChangeListener { position ->
                if (options.isPropertyEnabled(SHOW_TRANSITION)) {
                    viewer?.updateTransitionImage(binding.stylingPostersGridView.imageViews[position])
                }

                overlayView?.update(posters[position])
            }
            .withDismissListener { showShortToast(R.string.message_on_dismiss) }

        builder.withHiddenStatusBar(options.isPropertyEnabled(HIDE_STATUS_BAR))

        if (options.isPropertyEnabled(IMAGES_MARGIN)) {
            builder.withImagesMargin(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(CONTAINER_PADDING)) {
            builder.withContainerPadding(R.dimen.image_margin)
        }

        if (options.isPropertyEnabled(SHOW_TRANSITION)) {
            builder.withTransitionFrom(imageView)
        }

        builder.allowSwipeToDismiss(options.isPropertyEnabled(SWIPE_TO_DISMISS))
        builder.allowZooming(options.isPropertyEnabled(ZOOMING))

        if (options.isPropertyEnabled(SHOW_OVERLAY)) {
            setupOverlayView(posters, startPosition)
            builder.withOverlayView(overlayView)
        }

        if (options.isPropertyEnabled(RANDOM_BACKGROUND)) {
            builder.withBackgroundColor(getRandomColor())
        }

        viewer = builder.show()
    }

    private fun setupOverlayView(posters: MutableList<Poster>, startPosition: Int) {
        overlayView = PosterOverlayView(this).apply {
            update(posters[startPosition])

            onDeleteClick = {
                val currentPosition = viewer?.currentPosition() ?: 0

                posters.removeAt(currentPosition)
                viewer?.updateMedias(posters)

                posters.getOrNull(currentPosition)
                    ?.let { poster -> update(poster) }
            }
        }
    }

    private fun isVideo(poster: Poster?): Boolean {
        return poster?.url?.endsWith(".mp4") == true
    }

    private fun getMediaPath(poster: Poster?): String {
        return poster?.url ?: ""
    }

    private fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(
            255,
            random.nextInt(156),
            random.nextInt(156),
            random.nextInt(156)
        )
    }
}
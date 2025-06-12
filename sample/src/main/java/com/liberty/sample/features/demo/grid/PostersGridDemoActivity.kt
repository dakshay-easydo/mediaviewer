package com.liberty.sample.features.demo.grid

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.liberty.mediaviewer.MediaViewer
import com.liberty.sample.R
import com.liberty.sample.common.extensions.getDrawableCompat
import com.liberty.sample.common.extensions.loadImage
import com.liberty.sample.common.models.Demo
import com.liberty.sample.common.models.Poster
import com.liberty.sample.databinding.ActivityDemoPostersGridBinding

class PostersGridDemoActivity : AppCompatActivity() {

    private lateinit var viewer: MediaViewer<Poster>
    private lateinit var binding: ActivityDemoPostersGridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoPostersGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postersGridView.apply {
            imageLoader = ::loadPosterImage
            onPosterClick = ::openViewer
        }
    }

    private fun openViewer(startPosition: Int, target: ImageView) {
        viewer = MediaViewer
            .Builder(
                context = this,
                medias = Demo.posters,
                imageLoader = ::loadPosterImage
            )
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withImageChangeListener {
                viewer.updateTransitionImage(binding.postersGridView.imageViews[it])
            }
            .show()
    }

    private fun loadPosterImage(imageView: ImageView, poster: Poster?) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(poster?.url)
        }
    }
}
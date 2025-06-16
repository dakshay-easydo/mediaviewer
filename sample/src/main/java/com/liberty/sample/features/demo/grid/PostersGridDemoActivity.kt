package com.liberty.sample.features.demo.grid

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.liberty.mediaviewer.MediaViewer
import com.liberty.sample.R
import com.liberty.sample.common.extensions.getDrawableCompat
import com.liberty.sample.common.extensions.loadImage
import com.liberty.sample.common.models.Demo
import com.liberty.sample.common.models.Poster
import com.liberty.sample.databinding.ActivityDemoPostersGridBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        openViewer(1, ImageView(this))
    }

    private fun openViewer(startPosition: Int, target: ImageView) {
        val medias = Demo.mixed
        val item = medias[startPosition]
        viewer = MediaViewer
            .Builder(
                context = this,
                medias = listOf(item),
                isVideo = ::isVideo,
                getMediaPath = ::getMediaPath,
            )
            .withTransitionFrom(target)
            .withPageChangeListener { position, media ->
                viewer.updateTransitionImage(binding.postersGridView.imageViews[position])
            }
            .show()

        lifecycleScope.launch {
            delay(3000)
            viewer.updateMedias(medias)
        }
    }

    private fun loadPosterImage(imageView: ImageView, poster: Poster?) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(poster?.url)
        }
    }

    private fun isVideo(poster: Poster?): Boolean {
        return poster?.url?.endsWith(".mp4") == true
    }

    private fun getMediaPath(poster: Poster?): String {
        return poster?.url ?: ""
    }
}
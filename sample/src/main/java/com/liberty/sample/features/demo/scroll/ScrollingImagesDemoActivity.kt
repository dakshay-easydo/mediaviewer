package com.liberty.sample.features.demo.scroll

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.liberty.mediaviewer.MediaViewer
import com.liberty.sample.R
import com.liberty.sample.common.extensions.getDrawableCompat
import com.liberty.sample.common.extensions.loadImage
import com.liberty.sample.common.models.Demo
import com.liberty.sample.databinding.ActivityDemoScrollingImagesBinding

class ScrollingImagesDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoScrollingImagesBinding
    private val horizontalImageViews by lazy {
        listOf(
            binding.scrollingHorizontalFirstImage,
            binding.scrollingHorizontalSecondImage,
            binding.scrollingHorizontalThirdImage,
            binding.scrollingHorizontalFourthImage
        )
    }

    private val verticalImageViews by lazy {
        listOf(
            binding.scrollingVerticalFirstImage,
            binding.scrollingVerticalSecondImage,
            binding.scrollingVerticalThirdImage,
            binding.scrollingVerticalFourthImage
        )
    }

    private lateinit var viewer: MediaViewer<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDemoScrollingImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        horizontalImageViews.forEachIndexed { index, imageView ->
            loadImage(imageView, Demo.horizontalImages.getOrNull(index))
            imageView.setOnClickListener {
                openViewer(index, imageView, Demo.horizontalImages, horizontalImageViews)
            }
        }

        verticalImageViews.forEachIndexed { index, imageView ->
            loadImage(imageView, Demo.verticalImages.getOrNull(index))
            imageView.setOnClickListener {
                openViewer(index, imageView, Demo.verticalImages, verticalImageViews)
            }
        }
    }

    private fun openViewer(
        startPosition: Int,
        target: ImageView,
        images: List<String>,
        imageViews: List<ImageView>
    ) {
        viewer = MediaViewer.Builder<String>(
            context = this,
            medias = images,
            isVideo = ::isVideo,
            getMediaPath = ::getMediaPath,
        )
            .withStartPosition(startPosition)
            .withTransitionFrom(target)
            .withPageChangeListener { position, media ->
                viewer.updateTransitionImage(imageViews.getOrNull(position))
            }.show()
    }

    private fun loadImage(imageView: ImageView, url: String?) {
        imageView.apply {
            background = getDrawableCompat(R.drawable.shape_placeholder)
            loadImage(url)
        }
    }

    private fun isVideo(poster: String?): Boolean {
        return poster?.endsWith(".mp4") == true
    }

    private fun getMediaPath(poster: String?): String {
        return poster.orEmpty()
    }
}
package com.liberty.sample.common.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.liberty.sample.R
import com.liberty.sample.common.models.Demo
import com.liberty.sample.common.models.Poster
import com.liberty.sample.databinding.ViewPostersGridBinding

class PostersGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var binding : ViewPostersGridBinding
    var imageLoader: ((ImageView, Poster?) -> Unit)? = null
    var onPosterClick: ((Int, ImageView) -> Unit)? = null

    val imageViews by lazy {
        mapOf<Int, ImageView>(
            0 to binding.postersFirstImage,
            1 to binding.postersSecondImage,
            2 to binding.postersThirdImage,
            3 to binding.postersFourthImage,
            4 to binding.postersFifthImage,
            5 to binding.postersSixthImage,
            6 to binding.postersSeventhImage,
            7 to binding.postersEighthImage,
            8 to binding.postersNinthImage)
    }

    init {
        View.inflate(context, R.layout.view_posters_grid, this)
        binding = ViewPostersGridBinding.bind(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        imageViews.values.forEachIndexed { index, imageView ->
            imageLoader?.invoke(imageView, Demo.posters.getOrNull(index))
            imageView.setOnClickListener { onPosterClick?.invoke(index, imageView) }
        }
    }
}
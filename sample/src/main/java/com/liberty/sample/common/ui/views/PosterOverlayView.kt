package com.liberty.sample.common.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.liberty.sample.R
import com.liberty.sample.common.extensions.sendShareIntent
import com.liberty.sample.common.models.Poster
import com.liberty.sample.databinding.ViewPosterOverlayBinding

class PosterOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPosterOverlayBinding

    var onDeleteClick: (Poster) -> Unit = {}

    init {
        View.inflate(context, R.layout.view_poster_overlay, this)
        binding = ViewPosterOverlayBinding.bind(this)

        setBackgroundColor(Color.TRANSPARENT)
    }

    fun update(poster: Poster) {
        binding.posterOverlayDescriptionText.text = poster.description
        binding.posterOverlayShareButton.setOnClickListener { context.sendShareIntent(poster.url) }
        binding.posterOverlayDeleteButton.setOnClickListener { onDeleteClick(poster) }
    }
}
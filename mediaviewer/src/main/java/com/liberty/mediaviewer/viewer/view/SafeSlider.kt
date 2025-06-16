package com.liberty.mediaviewer.viewer.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.slider.Slider
import com.liberty.mediaviewer.common.extensions.ThemeUtils.supportsMaterial3

class SafeSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var slider: Slider? = null
    var seekBar: SeekBar? = null

    private var onChangeListener: ((value: Float, fromUser: Boolean) -> Unit)? = null

    var value: Float
        get() = slider?.value ?: (seekBar?.progress?.toFloat()?.div(seekBar?.max ?: 1) ?: 0f)
        set(v) {
            slider?.value = v
            seekBar?.progress = ((v * (seekBar?.max ?: 1))).toInt()
        }

    fun addOnChangeListener(listener: (value: Float, fromUser: Boolean) -> Unit) {
        onChangeListener = listener
    }

    init {
        if (context.supportsMaterial3()) {
            slider = Slider(context).apply {
                valueFrom = 0f
                valueTo = 1f
                stepSize = 0f
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
                addOnChangeListener { _, value, fromUser ->
                    onChangeListener?.invoke(value, fromUser)
                }
            }
            addView(slider)
        } else {
            seekBar = SeekBar(context).apply {
                max = 1000
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        onChangeListener?.invoke(progress.toFloat() / max.toFloat(), fromUser)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            addView(seekBar)
        }
    }
}

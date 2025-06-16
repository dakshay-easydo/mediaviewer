package com.liberty.mediaviewer.common.extensions

import android.content.Context
import android.graphics.Paint
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import com.liberty.mediaviewer.R

object ThemeUtils {

    /**
     * Checks if the current theme supports Material3 attributes.
     */
    fun Context.supportsMaterial3(): Boolean {
        return try {
            val attr = com.google.android.material.R.attr.textAppearanceBodyLarge
            val typed = obtainStyledAttributes(intArrayOf(attr))
            val has = typed.hasValue(0)
            typed.recycle()
            has
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Wraps context with your internal Material3-based viewer theme.
     */
    fun wrapWithViewerTheme(
        context: Context,
        @StyleRes themeResId: Int = R.style.ImageViewerDialog_Transparent
    ): Context {
        return ContextThemeWrapper(context, themeResId)
    }
}
package com.liberty.mediaviewer.viewer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.liberty.mediaviewer.common.extensions.resetScale

class ImageFragment : Fragment(), MediaAwareImpl {
    companion object {
        private const val ARG_PATH = "arg_path"
        private const val ARG_ZOOM = "arg_zoom"
        fun newInstance(
            path: String,
            zoomEnable: Boolean = true
        ) = ImageFragment().apply {
            arguments = bundleOf(
                ARG_PATH to path,
                ARG_ZOOM to zoomEnable
            )
        }
    }

    private val photoView by lazy {
        PhotoView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            isZoomable = arguments?.getBoolean(ARG_ZOOM) == true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Glide.with(this)
            .load(requireArguments().getString(ARG_PATH))
            .into(photoView)
        return photoView
    }

    override fun isScaled(): Boolean {
        return photoView.scale > 1.0
    }

    override fun scale(enable: Boolean) {
        if (isScaled()) resetScale(animate = true)
        photoView.isZoomable = enable
    }

    override fun resetScale(animate: Boolean) {
        photoView.resetScale(animate = animate)
    }

    override fun stopPlayback() {
        //No op
    }

    override fun release() {
        //No op
    }
}

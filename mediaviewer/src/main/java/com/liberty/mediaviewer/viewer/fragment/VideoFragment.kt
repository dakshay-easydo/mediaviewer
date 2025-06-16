package com.liberty.mediaviewer.viewer.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.liberty.mediaviewer.viewer.view.MultiTouchPlayer
import java.io.File

class VideoFragment : Fragment(), MediaAwareImpl {
    companion object {
        private const val ARG_PATH = "arg_path"
        private const val ARG_ZOOM = "arg_zoom"

        fun newInstance(
            path: String,
            zoomEnable: Boolean = true
        ) = VideoFragment().apply {
            arguments = bundleOf(
                ARG_PATH to path,
                ARG_ZOOM to zoomEnable
            )
        }
    }

    private var player: ExoPlayer? = null
    private lateinit var playerView: MultiTouchPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playerView = MultiTouchPlayer(requireContext())
        return playerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val path = requireArguments().getString(ARG_PATH) ?: return
        playerView.zoomEnable = requireArguments().getBoolean(ARG_ZOOM)
        val uri = if (path.startsWith("http")) Uri.parse(path) else Uri.fromFile(File(path))
        val exoPlayer = ExoPlayer.Builder(requireContext()).build().also {
            val mediaItem = MediaItem.fromUri(uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
        }
        this.player = exoPlayer
        playerView.setPlayer(exoPlayer)
    }

    override fun isScaled(): Boolean {
        return playerView.isZoomed()
    }

    override fun scale(enable: Boolean) {
        if (!enable) playerView.resetZoom(animated = true)
//        playerView.zoomEnable = enable
    }

    override fun resetScale(animate: Boolean) {
        playerView.resetZoom(animated = animate)
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun stopPlayback() {
        player?.pause()
    }

    override fun release() {
        player?.playWhenReady = false
        player?.pause()
        player?.release()
        player = null
    }

    override fun onDestroyView() {
        release()
        super.onDestroyView()
    }
}

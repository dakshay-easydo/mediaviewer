package com.liberty.mediaviewer.viewer.fragment;

interface MediaAwareImpl {
    fun isScaled(): Boolean
    fun scale(enable: Boolean)
    fun resetScale(animate: Boolean)
    fun stopPlayback()
    fun release()
}
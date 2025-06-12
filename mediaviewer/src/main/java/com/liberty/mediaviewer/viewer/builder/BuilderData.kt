/*
 * Copyright 2018 Liberty Infospace
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liberty.mediaviewer.viewer.builder

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import com.liberty.mediaviewer.listeners.OnDismissListener
import com.liberty.mediaviewer.listeners.OnMediaChangeListener
import com.liberty.mediaviewer.loader.ImageLoader

class BuilderData<T>(
    val medias: List<T>,
    val imageLoader: ImageLoader<T>,
    val isVideo: ((T) -> Boolean)? = null,
    val getMediaPath: ((T) -> String)? = null,
) {
    var backgroundColor = Color.BLACK
    var startPosition: Int = 0
    var mediaChangeListener: OnMediaChangeListener? = null
    var onDismissListener: OnDismissListener? = null
    var overlayView: View? = null
    var imageMarginPixels: Int = 0
    var containerPaddingPixels = IntArray(4)
    var shouldStatusBarHide = true
    var isZoomingAllowed = true
    var isSwipeToDismissAllowed = true
    var transitionView: ImageView? = null
}
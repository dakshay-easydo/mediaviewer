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

package com.liberty.mediaviewer.listeners;

/**
 * Interface definition for a callback to be invoked when current image position was changed.
 */
//N.B.! This class is written in Java for convenient use of lambdas due to languages compatibility issues.
public interface OnPageChangeListener<T> {
    void onPageChange(int position, T t);
}
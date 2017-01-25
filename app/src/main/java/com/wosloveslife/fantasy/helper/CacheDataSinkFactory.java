/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wosloveslife.fantasy.helper;

import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;

/**
 * A {@link DataSink.Factory} that produces {@link CacheDataSink}.
 */
public final class CacheDataSinkFactory implements DataSink.Factory {
    private final TransferListener<? super CacheDataSink> listener;

    private final Cache cache;
    private final long maxCacheFileSize;

    /**
     * @see CacheDataSink#CacheDataSink(Cache, long)
     */
    public CacheDataSinkFactory(Cache cache, long maxCacheFileSize, TransferListener<? super CacheDataSink> listener) {
        this.cache = cache;
        this.maxCacheFileSize = maxCacheFileSize;
        this.listener = listener;
    }

    @Override
    public DataSink createDataSink() {
        return new CacheDataSink(cache, maxCacheFileSize, 0, listener);
    }
}

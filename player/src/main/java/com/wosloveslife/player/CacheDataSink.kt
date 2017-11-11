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
package com.wosloveslife.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.Cache.CacheException
import com.google.android.exoplayer2.util.ReusableBufferedOutputStream
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Writes data into a cache.
 */
class CacheDataSink(
        private val cache: Cache,
        private val maxCacheFileSize: Long,
        private val bufferSize: Int = 0,
        private var listener: TransferListener<in CacheDataSink>?
) : DataSink {

    private var dataSpec: DataSpec? = null
    private var file: File? = null
    private var outputStream: OutputStream? = null
    private var underlyingFileOutputStream: FileOutputStream? = null
    private var outputStreamBytesWritten: Long = 0
    private var dataSpecBytesWritten: Long = 0
    private var bufferedOutputStream: ReusableBufferedOutputStream? = null

    /**
     * Thrown when IOException is encountered when writing data into sink.
     */
    class CacheDataSinkException(cause: IOException) : CacheException(cause)

    /**
     * @param cache            The cache into which data should be written.
     * @param maxCacheFileSize The maximum size of a cache file, in bytes. If the sink is opened for
     * a [DataSpec] whose size exceeds this value, then the data will be fragmented into
     * multiple cache files.
     * @param bufferSize       The buffer size in bytes for writing to a cache file. A zero or negative
     * value disables buffering.
     */
    @JvmOverloads constructor(cache: Cache, maxCacheFileSize: Long, bufferSize: Int = 0) : this(cache, maxCacheFileSize, 0, null) {}

    @Throws(CacheDataSinkException::class)
    override fun open(dataSpec: DataSpec) {
        this.dataSpec = dataSpec
        if (dataSpec.length == C.LENGTH_UNSET.toLong()) {
            return
        }
        dataSpecBytesWritten = 0
        try {
            openNextOutputStream()
        } catch (e: IOException) {
            throw CacheDataSinkException(e)
        }

        if (listener != null) {
            listener!!.onTransferStart(this, dataSpec)
        }
    }

    @Throws(CacheDataSinkException::class)
    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        if (dataSpec?.length == C.LENGTH_UNSET.toLong()) {
            return
        }
        var bytesWritten = 0
        while (bytesWritten < length) {
            if (outputStreamBytesWritten == maxCacheFileSize) {
                closeCurrentOutputStream()
                openNextOutputStream()
            }
            val bytesToWrite = Math.min((length - bytesWritten).toLong(), maxCacheFileSize - outputStreamBytesWritten).toInt()
            outputStream!!.write(buffer, offset + bytesWritten, bytesToWrite)
            bytesWritten += bytesToWrite
            outputStreamBytesWritten += bytesToWrite.toLong()
            dataSpecBytesWritten += bytesToWrite.toLong()

            if (listener != null) {
                listener?.onBytesTransferred(this, bytesToWrite)
            }
        }
    }

    @Throws(CacheDataSinkException::class)
    override fun close() {
        if (dataSpec == null || dataSpec!!.length == C.LENGTH_UNSET.toLong()) {
            return
        }
        try {
            closeCurrentOutputStream()
        } catch (e: IOException) {
            throw CacheDataSinkException(e)
        }
    }

    @Throws(IOException::class)
    private fun openNextOutputStream() {
        file = cache.startFile(
                dataSpec?.key,
                dataSpec!!.absoluteStreamPosition + dataSpecBytesWritten,
                Math.min(dataSpec!!.length - dataSpecBytesWritten, maxCacheFileSize))

        underlyingFileOutputStream = FileOutputStream(file!!)
        if (bufferSize > 0) {
            if (bufferedOutputStream == null) {
                bufferedOutputStream = ReusableBufferedOutputStream(underlyingFileOutputStream, bufferSize)
            } else {
                bufferedOutputStream!!.reset(underlyingFileOutputStream)
            }
            outputStream = bufferedOutputStream
        } else {
            outputStream = underlyingFileOutputStream
        }
        outputStreamBytesWritten = 0
    }

    @Throws(IOException::class)
    private fun closeCurrentOutputStream() {
        if (outputStream == null) {
            return
        }

        var success = false
        try {
            outputStream!!.flush()
            underlyingFileOutputStream!!.fd.sync()
            success = true
        } finally {
            Util.closeQuietly(outputStream)
            outputStream = null
            val fileToCommit = file
            file = null
            if (success) {
                cache.commitFile(fileToCommit)
            } else {
                fileToCommit!!.delete()
            }
            if (listener != null) {
                listener!!.onTransferEnd(this)
            }
        }
    }
}
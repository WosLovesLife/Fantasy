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

import android.net.Uri

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener

import java.io.EOFException
import java.io.IOException
import java.io.RandomAccessFile

/**
 * A [DataSource] for reading local files.
 */
class FileDataSource
/**
 * @param listener An optional listener.
 */
@JvmOverloads constructor(private val listener: TransferListener<in FileDataSource>? = null) : DataSource {

    private var file: RandomAccessFile? = null
    private var uri: Uri? = null
    private var bytesRemaining: Long = 0
    private var opened: Boolean = false

    /**
     * Thrown when IOException is encountered during local file read operation.
     */
    class FileDataSourceException(cause: IOException) : IOException(cause)

    @Throws(FileDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        try {
            uri = dataSpec.uri
            file = RandomAccessFile(dataSpec.uri.path, "r")
            file!!.seek(dataSpec.position)
            bytesRemaining = if (dataSpec.length == C.LENGTH_UNSET.toLong())
                file!!.length() - dataSpec.position
            else
                dataSpec.length
            if (bytesRemaining < 0) {
                throw EOFException()
            }
        } catch (e: IOException) {
            throw FileDataSourceException(e)
        }

        opened = true
        listener?.onTransferStart(this, dataSpec)

        return bytesRemaining
    }

    @Throws(FileDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        if (readLength == 0) {
            return 0
        } else if (bytesRemaining == 0L) {
            return C.RESULT_END_OF_INPUT
        } else {
            val bytesRead: Int
            try {
                bytesRead = file!!.read(buffer, offset, Math.min(bytesRemaining, readLength.toLong()).toInt())
            } catch (e: IOException) {
                throw FileDataSourceException(e)
            }

            if (bytesRead > 0) {
                bytesRemaining -= bytesRead.toLong()
                listener?.onBytesTransferred(this, bytesRead)
            }

            return bytesRead
        }
    }

    override fun getUri(): Uri? {
        return uri
    }

    @Throws(FileDataSourceException::class)
    override fun close() {
        uri = null
        try {
            if (file != null) {
                file!!.close()
            }
        } catch (e: IOException) {
            throw FileDataSourceException(e)
        } finally {
            file = null
            if (opened) {
                opened = false
                listener?.onTransferEnd(this)
            }
        }
    }
}

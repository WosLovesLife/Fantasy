package com.wosloveslife.player

import android.net.Uri

/**
 * Created by zhangh on 2017/11/10.
 */
class AudioResource(
        var mId: String,
        var mTitle: String?,
        var artist: String?,
        var album: String?,
        var path: Uri,
        var duration: Long,
        var size: Long
)
package com.wosloveslife.player

/**
 * Created by zhangh on 2017/11/11.
 */
class PlayerException : RuntimeException {
    companion object ErrorCode {
        const val NO_ERROR = 0
        const val NO_AUDIO = 1
        const val FROM_EXO_PLAYER = 2
    }

    private var mErrorCode: Int = NO_ERROR

    constructor(errorCode: Int) : super() {
        mErrorCode = errorCode
    }

    constructor(errorCode: Int, message: String?) : super(message) {
        mErrorCode = errorCode
    }

    constructor(errorCode: Int, message: String?, cause: Throwable?) : super(message, cause) {
        mErrorCode = errorCode
    }

    constructor(errorCode: Int, cause: Throwable?) : super(cause) {
        mErrorCode = errorCode
    }

    constructor(errorCode: Int, message: String?, cause: Throwable?, enableSuppression: Boolean,
                writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace) {
        mErrorCode = errorCode
    }
}
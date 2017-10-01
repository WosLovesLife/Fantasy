package com.wosloveslife.fantasy.v2.player;

/**
 * Created by zhangh on 2017/9/24.
 */

public class PlayerException extends RuntimeException {
    public @interface ErrorCode {
        int NO_ERROR = 0;
        int MUSIC_IS_NULL = 1;
    }

    private int mErrorCode;

    public PlayerException(@ErrorCode int errorCode) {
        mErrorCode = errorCode;
    }

    public PlayerException(@ErrorCode int errorCode, String message) {
        super(message);
    }

    public PlayerException(@ErrorCode int errorCode, Throwable cause) {
        super(cause);
    }

    public PlayerException(@ErrorCode int errorCode, String message, Throwable cause) {
        super(message, cause);
    }
}

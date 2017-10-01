/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package com.wosloveslife.fantasy.event;

public class Event {
    private static int totalEvents = 1;

    public static final int REFRESH = totalEvents++;
    public static final int SHEET_SCANNED = totalEvents++;
    public static final int SHEET_LOADED = totalEvents++;

    private int mAction;
    private Object[] mData;

    private Event(int action, Object... data) {
        mAction = action;
        mData = data;
    }

    public static Event create(int action, Object... data) {
        return new Event(action, data);
    }

    public int getAction() {
        return mAction;
    }

    public Object[] getData() {
        return mData;
    }
}

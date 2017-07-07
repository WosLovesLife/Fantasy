package com.wosloveslife.dao;

import android.support.annotation.IntDef;

/**
 * Created by leonard on 17/6/17.
 */

public interface SheetProperties {
    String ID = "id";

    String TITLE = "title";
    String AUTHOR = "author";
    String TITLE_PINYIN = "titlePinyin";
    String AUTHOR_PINYIN = "authorPinyin";
    String SONGS = "songs";
    String CREATE_TIMESTAMP = "createTimestamp";
    String MODIFY_TIMESTAMP = "modifyTimestamp";
    String TYPE = "type";
    String STATE = "state";
    String PATH = "path";

    int TYPE_DEF = 0;
    int TYPE_DIR = 1;
    int TYPE_CUSTOM = 2;

    @IntDef({TYPE_DEF, TYPE_DIR, TYPE_CUSTOM})
    public @interface Type {
    }

    int STATE_NORMAL = 0;
    int STATE_FILTERED = 1;

    @IntDef({STATE_NORMAL, STATE_FILTERED})
    public @interface State {
    }
}

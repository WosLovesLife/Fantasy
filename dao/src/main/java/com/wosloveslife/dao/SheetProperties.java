package com.wosloveslife.dao;

import android.support.annotation.IntDef;

import static com.wosloveslife.dao.SheetProperties.State.FILTERED;
import static com.wosloveslife.dao.SheetProperties.State.NORMAL;
import static com.wosloveslife.dao.SheetProperties.Type.CUSTOM;
import static com.wosloveslife.dao.SheetProperties.Type.DEF;
import static com.wosloveslife.dao.SheetProperties.Type.DIR;

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

    @IntDef({DEF, DIR, CUSTOM})
    public @interface Type {
        int DEF = 0;
        int DIR = 1;
        int CUSTOM = 2;
    }

    @IntDef({NORMAL, FILTERED})
    public @interface State {
        int NORMAL = 0;
        int FILTERED = 1;
    }
}

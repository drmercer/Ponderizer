package net.danmercer.ponderizer.scriptureview;

import android.content.Context;
import android.content.Intent;

import net.danmercer.ponderizer.Scripture;

/**
 * Created by Dan on 11/7/2015.
 */
public class ScriptureIntent extends Intent {
    // Used when putting a Scripture into an intent as a parcelable extra
    public static final String EXTRA_SCRIPTURE = "net.danmercer.ponderizer.SCRIPTURE";

    public ScriptureIntent(Context packageContext, Class<?> cls, Scripture s) {
        super(packageContext, cls);
        putExtra(EXTRA_SCRIPTURE, s);
    }
}

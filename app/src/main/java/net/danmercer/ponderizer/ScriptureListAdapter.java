package net.danmercer.ponderizer;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Dan on 10/20/2015.
 */
public class ScriptureListAdapter extends SimpleAdapter {

    public ScriptureListAdapter(Context context, List<HashMap<String, Object>> list, int layout_res, String[] from, int[] to) {
        super(context, list, layout_res, from, to);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}

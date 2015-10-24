package net.danmercer.ponderizer.scriptureview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

/**
 * Created by Dan on 10/24/2015.
 */
public class ScriptureTextFragment extends Fragment {
    private static final String ARGSKEY_SCRIPTURE = "ScriptureTextFrag.scripture";
    private Scripture scripture;

    public static ScriptureTextFragment createNew(Scripture scripture) {
        ScriptureTextFragment f = new ScriptureTextFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGSKEY_SCRIPTURE, scripture);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_scripture_text, container, false);
        // just in case I change the layout in the future:
        TextView tv = (TextView) view.findViewById(R.id.scripture_text);

        Bundle args = getArguments();
        scripture = args.getParcelable(ARGSKEY_SCRIPTURE);
        tv.setText(scripture.getBody());

        return view;
    }
}

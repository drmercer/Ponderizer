/*
 * Copyright 2015. Dan Mercer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.danmercer.ponderizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by Dan on 11/4/2015.
 */
public class ScriptureMasteryHelper {
    public static void showDialog(final Context context) {
        final AlertDialog.Builder db = new AlertDialog.Builder(context);
        db.setTitle(R.string.sm_dialog_message);
        final boolean[] choices = new boolean[4];
        db.setMultiChoiceItems(R.array.sm_collections, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                choices[which] = isChecked;
            }
        });
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                importScriptureMasteries(context, choices);
            }
        };
        db.setPositiveButton(R.string.import_sm, l);
        db.setNegativeButton(R.string.cancel, null);
        db.show();
    }

    private static void importScriptureMasteries(Context c, boolean[] choices) {
        Resources res = c.getResources();
        if (choices[0]) {
            // Import Old Testament list
            String[] refs = res.getStringArray(R.array.sm_ot_refs);
            String[] texts = res.getStringArray(R.array.sm_ot_texts);
            loadScripList(c, refs, texts);
        }
        if (choices[1]) {
            // Import New Testament list
            String[] refs = res.getStringArray(R.array.sm_nt_refs);
            String[] texts = res.getStringArray(R.array.sm_nt_texts);
            loadScripList(c, refs, texts);
        }
        if (choices[2]) {
            // Import Book of Mormon list
            String[] refs = res.getStringArray(R.array.sm_bom_refs);
            String[] texts = res.getStringArray(R.array.sm_bom_texts);
            loadScripList(c, refs, texts);
        }
        if (choices[3]) {
            // Import Old Testament list
            String[] refs = res.getStringArray(R.array.sm_dnc_refs);
            String[] texts = res.getStringArray(R.array.sm_dnc_texts);
            loadScripList(c, refs, texts);
        }
    }

    private static void loadScripList(Context c, String[] refs, String[] texts) {
        for (int i = 0; i < refs.length; i++) {
            String ref = refs[i];
            String text = texts[i];
            new Scripture(ref, text, NewMainActivity.Category.IN_PROGRESS)
                    .writeToFile(c);
        }
    }
}

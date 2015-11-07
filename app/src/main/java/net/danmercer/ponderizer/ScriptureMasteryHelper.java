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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Dan on 11/4/2015.
 */
public class ScriptureMasteryHelper {
    public static final int SCRIPTURES_PER_COLLECTION = 25;
    private static int[] REFS_IDS = {R.array.sm_ot_refs, R.array.sm_nt_refs, R.array.sm_bom_refs, R.array.sm_dnc_refs};
    private static int[] TEXTS_IDS = {R.array.sm_ot_texts, R.array.sm_nt_texts, R.array.sm_bom_texts, R.array.sm_dnc_texts};
    public static final int NUM_OF_COLLECTIONS = REFS_IDS.length;

    /**
     * @param context The Context to show the AlertDialog in
     * @param r       A Runnable to run after the scriptures are imported.
     */
    public static void showDialog(final Context context, final Runnable r) {
        final AlertDialog.Builder db = new AlertDialog.Builder(context);
        db.setTitle(R.string.sm_dialog_title);

        // Set up multi-choice list in dialog.
        final boolean[] choices = new boolean[4];
        db.setMultiChoiceItems(R.array.sm_collections, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                choices[which] = isChecked;
                // Disable buttons unless at least one is clicked.
                boolean readyToGo = choices[0] || choices[1] || choices[2] || choices[3];
                ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE).setEnabled(readyToGo);
                ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEUTRAL).setEnabled(readyToGo);
            }
        });
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("SM Dialog", "onClick called, which = " + which);
                if (which == Dialog.BUTTON_POSITIVE) {
                    importScriptureMasteries(context, choices);
                    r.run();
                } else if (which == Dialog.BUTTON_NEUTRAL) {
                    // Offer to remove the scriptures in those lists from the user's list
                    promptRemoveSMs(context, choices, r);
                }
            }
        };

        // Set up buttons
        db.setPositiveButton(R.string.import_sm, l);
        db.setNeutralButton(R.string.remove_sm, l);
        db.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = db.create();
        dialog.show();
        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    private static void promptRemoveSMs(final Context context, final boolean[] choices, final Runnable r) {
        int totalRefCount = 0;
        for (boolean b : choices) {
            if (b) totalRefCount += SCRIPTURES_PER_COLLECTION;
        }
        final ArrayList<String> filenames = new ArrayList<>();
        for (int i = 0; i < NUM_OF_COLLECTIONS; i++) {
            if (choices[i]) {
                String[] refs = context.getResources().getStringArray(REFS_IDS[i]);
                for (int srcIdx = 0; srcIdx < SCRIPTURES_PER_COLLECTION; srcIdx++){
                    String filename = Scripture.convertRefToFilename(refs[srcIdx]);
                    filenames.add(filename);
                }
            }
        }
        NewMainActivity.Category[] categories = NewMainActivity.Category.values();
        final ArrayList<File> filesToDelete = new ArrayList<>();
        for (NewMainActivity.Category category : categories) {
            File dir = Scripture.getDir(context, category);
            if (!dir.exists()) continue;
            File[] files = dir.listFiles();
            for (File f : files) {
                String fName = f.getName();
                if (filenames.contains(fName)) {
                    filesToDelete.add(f);
                }
            }
        }

        AlertDialog.Builder db = new AlertDialog.Builder(context);
        int count = filesToDelete.size();
        db.setMessage(context.getResources().getString(R.string.sm_remove_prompt, count));
        db.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the scriptures in filenames
                for (File f : filesToDelete) {
                    f.delete();
                }
                r.run(); // Run the runnable passed to showDialog()
            }
        });
        db.setNegativeButton(R.string.no, null);
        db.show();
    }

    private static void importScriptureMasteries(Context c, boolean[] choices) {
        Resources res = c.getResources();
        for (int i = 0; i < NUM_OF_COLLECTIONS; i++) {
            if (choices[i]) {
                // Import scripture list
                String[] refs = res.getStringArray(REFS_IDS[i]);
                String[] texts = res.getStringArray(TEXTS_IDS[i]);
                for (int j = 0; j < refs.length; j++) {
                    String ref = refs[j];
                    String text = texts[j];
                    new Scripture(ref, text, NewMainActivity.Category.IN_PROGRESS)
                            .writeToFile(c);
                }
            }
        }
    }

}

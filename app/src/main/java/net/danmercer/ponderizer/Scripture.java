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
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import net.danmercer.ponderizer.memorize.MemorizeActivity;
import net.danmercer.ponderizer.scriptureview.AddNoteActivity;
import net.danmercer.ponderizer.scriptureview.ScriptureViewActivity;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Dan on 10/22/2015.
 */
public class Scripture implements Parcelable {

    // Used when putting a Scripture into an intent as a parcelable extra
    public static final String EXTRA_SCRIPTURE = Scripture.class.getName();

    // The name of the directory for the notes files
    public static final String NOTES_DIR = "notes";

    /**
     * CREATOR used by the Android OS to reconstruct a Scripture object that has been stored in a
     * Parcel
     */
    public static final Parcelable.Creator<Scripture> CREATOR = new Creator<Scripture>() {
        @Override
        public Scripture createFromParcel(Parcel source) {
            String reference = source.readString();
            String body = source.readString();
            NewMainActivity.Category cat = (NewMainActivity.Category) source.readSerializable();
            return new Scripture(reference, body, cat);
        }

        @Override
        public Scripture[] newArray(int size) {
            return new Scripture[size];
        }
    };

    public final String reference;
    public final String filename;
    public final String body;
    private NewMainActivity.Category mCategory;

    public static LinkedList<Scripture> loadScriptures(@NonNull Context c, NewMainActivity.Category category) {
        File dir = c.getDir(category.name(), Context.MODE_PRIVATE);

        File[] files = dir.listFiles();
        LinkedList<Scripture> scriptures = new LinkedList<>();
        for (File f : files) {
            if (f.isFile()) {
                try {
                    final long numOfChars = f.length() / 2;
                    long numRead = 0;
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String reference = br.readLine();
                    numRead = reference.length() + 1; // length of first line, plus one for the newline.
                    StringBuilder body = new StringBuilder();
                    while (numRead < numOfChars) {
                        try {
                            String line = br.readLine();
                            if (line == null) {
                                break;
                            }
                            body.append(line).append("\n");
                        } catch (EOFException e) {
                            break;
                        }
                    }

                    // Add the new scripture
                    Scripture s = new Scripture(reference, body.toString(), category);
                    scriptures.add(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return scriptures;
    }

    public Scripture(@NonNull String reference, @NonNull String body,
                     @NonNull NewMainActivity.Category cat) {
        this.reference = reference.trim(); // Remove excess whitespace
        // Convert reference to a proper filename by
        // (1) replacing colons with dots,
        // (2) removing any whitespace or other illegal characters, and
        // (3) appending ".txt" to the end to make it a text file.
        this.filename = this.reference.toLowerCase()
                .replace(':', '.')
                .replaceAll("[\\s\\\\\\?/\\*\"<>]", "")
                .concat(".txt");

        // Remove excess whitespace from the body text as well.
        this.body = body.trim();

        // This part's simple enough. :)
        this.mCategory = cat;
    }

    /**
     * Writes the contained scripture to a file in the appropriate directory.
     */
    public boolean writeToFile(Context c) {
        File dir = getDir(c, mCategory);
        File dest = new File(dir, filename);
        if (!dest.exists()) {
            try {
                dest.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            FileWriter fw = new FileWriter(dest);
            fw.write(reference + "\n" +
                    body + "\n");
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the scripture reference
     */
    @Override
    public String toString() {
        return reference;
    }

    // From Parcelable interface
    @Override
    public int describeContents() {
        return 0;
    }

    // From Parcelable interface
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reference);
        dest.writeString(body);
        dest.writeSerializable(mCategory);
    }

    /**
     * Returns the scripture reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns the text of the scripture
     */
    public String getBody() {
        return body;
    }

    public String getFilename() {
        return filename;
    }

    public NewMainActivity.Category getCategory() {
        return mCategory;
    }


    public void changeCategory(Context c, NewMainActivity.Category category) {
        if (category != mCategory) {
            // Delete file from old category directory
            File dir = getDir(c, mCategory);
            File file = new File(dir, filename);
            if (file.exists()) file.delete();

            // Write file to new category directory
            this.mCategory = category;
            writeToFile(c);
        }
    }

    public boolean isCompleted() {
        return mCategory == NewMainActivity.Category.COMPLETED;
    }

    /**
     * @param context
     * @param r       The Runnable to run after the user confirms the deletion
     */
    public void deleteWithConfirmation(final Context context, final Runnable r) {
        AlertDialog.Builder db = new AlertDialog.Builder(context);
        db.setMessage(
                "Are you sure you want to delete this scripture and its notes from your list?");
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Scripture.this.delete(context);
                r.run();
            }
        };
        db.setPositiveButton(R.string.yes, l);
        db.setNegativeButton(R.string.no, null);
        db.show();
    }

    // Deletes this scripture
    public void delete(Context context) {
        // Delete scripture file
        File scripDir = getDir(context, mCategory);
        File scripFile = new File(scripDir, filename);
        if (scripFile.exists()) scripFile.delete();
        // Delete notes file
        File notesDir = context.getDir(Scripture.NOTES_DIR, 0);
        File noteFile = new File(notesDir, filename);
        if (noteFile.exists()) noteFile.delete();

        ScriptureAppWidget.updateAllAppWidgets(context);
    }

    public boolean fileExists(Context context) {
        File scripDir = getDir(context, mCategory); // TODO: left off here, extract method (to use in SMHelper)
        File scripFile = new File(scripDir, filename);
        return scripFile.exists();
    }

    public static File getDir(Context context, NewMainActivity.Category category) {
        return context.getDir(category.name(), 0);
    }

    public boolean hasNotes(Context c) {
        File notesFile = getNotesFile(c);
        return notesFile.exists(); // The file should not exist if there are no notes.
    }

    public File getNotesFile(Context c) {
        return new File(c.getDir(Scripture.NOTES_DIR, 0), filename);
    }

    // INTENT UTILITY METHODS, called by ScriptureAppWidget and WidgetPopupMenuActivity

    public Intent getMemorizeIntent(Context context) {
        Intent i = new Intent(context, MemorizeActivity.class);
        i.putExtra(Scripture.EXTRA_SCRIPTURE, this);
        return i;
    }

    public Intent getAddNoteIntent(Context context) {
        Intent i = new Intent(context, AddNoteActivity.class);
        i.putExtra(Scripture.EXTRA_SCRIPTURE, this);
        return i;
    }

    public Intent getViewIntent(Context context) {
        Intent i = new Intent(context, ScriptureViewActivity.class);
        i.putExtra(Scripture.EXTRA_SCRIPTURE, this);
        return i;
    }
}

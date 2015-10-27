package net.danmercer.ponderizer;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    // The name of the directory where this scripture is stored. Will be important once I implement
    // multiple lists.
    public static final String CATEGORY_PRESENT = "present";

    /**
     * CREATOR used by the Android OS to reconstruct a Scripture object that has been stored in a
     * Parcel
     */
    public static final Parcelable.Creator<Scripture> CREATOR = new Creator<Scripture>() {
        @Override
        public Scripture createFromParcel(Parcel source) {
            String reference = source.readString();
            String body = source.readString();
            return new Scripture(reference, body);
        }

        @Override
        public Scripture[] newArray(int size) {
            return new Scripture[size];
        }
    };

    public final String reference;
    public final String filename;
    public final String body;

    public static LinkedList<Scripture> loadScriptures(@NonNull File dir) {
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
                    scriptures.add(new Scripture(reference, body.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return scriptures;
    }

    public Scripture(@NonNull String reference, @NonNull String body) {
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
    }

    /**
     * Writes the contained scripture to a file in the given directory.
     */
    public boolean writeToFile(File dir) {
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

    /** Returns the scripture reference */
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
    }

    /** Returns the scripture reference */
    public String getReference() {
        return reference;
    }

    /** Returns the text of the scripture */
    public String getBody() {
        return body;
    }

    public String getFilename() {
        return filename; // TODO: return the notes filename
    }
}

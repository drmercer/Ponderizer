package net.danmercer.ponderizer;

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
public class Scripture {
    private final String reference;
    private final String filename;
    private final String body;

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

    public Scripture (@NonNull String reference, @NonNull String body) {
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

    @Override
    public String toString() {
        return reference;
    }
}

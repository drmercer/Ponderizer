package net.danmercer.ponderizer.scriptureview;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dan on 10/26/2015.
 */
public class Note {
    public static final String EXTRA_NOTE_TIME = "Note.time";
    public static final String EXTRA_NOTE_TEXT = "Note.text";
    private static final String DATE_FORMAT_STRING = "dd LLL yyyy";
    private static DateFormat instance = null;

    public static DateFormat getDateFormat() {
        if (instance == null) {
            instance = new SimpleDateFormat(DATE_FORMAT_STRING + ", h:mma", Locale.getDefault());
        }
        return instance;
    }

    public static String format(long time) {
        return getDateFormat().format(new Date(time));
    }

    public static void loadFromFile(File f, List<Note> list) {
        // Make sure the given File exists and represents a file (not a directory)
        list.clear(); // Empty the list to prepare to reload the Notes.
        if (!f.exists()) {
            // File doesn't exist, no notes have been created.
            return; // Return, leaving the list empty.
        } else if (!f.isFile()) {
            // If it exists but is not a file, throw exception
            throw new IllegalArgumentException("f must be an existing file.");
        }

        try {
            Log.d("Noteload", "Loading notes from file " + f.getAbsolutePath() + "//" + f.getName());
            final long numOfChars = f.length() / 2;
            long numRead = 0;
            BufferedReader br = new BufferedReader(new FileReader(f));
            long timestamp = 0;
            StringBuilder text = new StringBuilder();
            while (numRead < numOfChars) {
                String line = br.readLine();
                boolean eof = line == null;
                boolean newNote = !eof && line.startsWith("# ");

                // We might have just finished reading in a note
                if (eof || newNote) {
                    // If we just finished a note...
                    if (timestamp != 0) {
                        // then pack previous note into a Note object.
                        Note prev = new Note(timestamp, text.toString().trim());
                        list.add(prev); // Add it to the list
                    }
                }
                if (eof) { // End of file, so break
                    break;
                }
                if (newNote) { // Begins a new Note
                    text.delete(0, text.length()); // Empty the StringBuilder
                    // and parse the timeString into a timestamp (long)
                    String timeString = line.substring(2, line.length());
                    try {
                        timestamp = getDateFormat().parse(timeString).getTime();
                    } catch (ParseException e) {
                        Log.e("Note", "Corrupted note in file " + f.getName(), e);
                        timestamp = 0; // This will make the loop discard this note when it
                        // reaches the next note.
                    }
                } else {
                    // Continues a Note
                    text.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeNotesToFile(List<Note> notes, File f) {
        if (notes.isEmpty()) {
            // If the notes list is empty, delete the file
            f.delete();
            return;
        }
        FileWriter fw = null;
        try {
            f.createNewFile();
            fw = new FileWriter(f, false); // Overwrite existing file
            for (Note n : notes) {
                n.writeToFile(fw);
            }
        } catch (IOException e) {
            Log.e("AddNoteActivity", "Couldn't save Note properly", e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Utility method, called by AddNoteActivity.saveAndFinish() when launched from an app widget
    public static void writeNoteToFile(long timestamp, String note, File f) {
        FileWriter fw = null;
        try {
            f.createNewFile();
            fw = new FileWriter(f, true);
            new Note(timestamp, note).writeToFile(fw);
        } catch (IOException e) {
            Log.e("AddNoteActivity", "Couldn't save Note properly", e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("AddNoteActivity", "Couldn't save Note properly", e);
                }
            }
        }
    }

    private long mTimestamp;
    private String mTimeString;
    private final String mText;

    public Note(long timestamp, String text) {
        mText = text.replaceAll("(?:^#|(?<=\\n)#)", "");
        // Gets rid of any # symbols at the beginning of lines, because # denotes the beginning
        // of a note in the text file

        setTimestamp(timestamp);
    }

    // Returns the string that describes when this note was created or edited
    @Override
    public String toString() {
        return getTimeString();
    }

    public String getText() {
        return mText;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
        mTimeString = format(timestamp);
    }

    public void writeToFile(FileWriter fw) throws IOException {
        // Write note header ("# <timeStamp>")
        fw.write("# ");
        fw.write(mTimeString);
        fw.write("\n");

        // Write note text
        fw.write(mText);
        fw.write("\n\n");
    }

    public String getTimeString() {
        return mTimeString;
    }

    public String getDateString() {
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date(mTimestamp));
    }
}

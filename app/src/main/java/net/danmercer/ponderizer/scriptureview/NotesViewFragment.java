package net.danmercer.ponderizer.scriptureview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dan on 10/24/2015.
 */
public class NotesViewFragment extends Fragment {
    public static class Note {
        private static DateFormat instance = null;
        public static DateFormat getDateFormat() {
            if (instance == null) {
                instance = new SimpleDateFormat("dd LLL yyyy, h:mma", Locale.getDefault());
            }
            return instance;
        }

        public static List<Note> loadFromFile(File f) {
            // Make sure the given File exists and represents a file (not a directory)
            if (!f.exists()) {
                // File doesn't exist, no notes have been created.
                return new LinkedList<Note>();
            } else if (!f.isFile()) {
                // If it exists but is not a file, throw exception
                throw new IllegalArgumentException("f must be an existing file.");
            }

            LinkedList<Note> notes = new LinkedList<>();
            try {
                final long numOfChars = f.length() / 2;
                long numRead = 0;
                BufferedReader br = new BufferedReader(new FileReader(f));
                long timestamp = 0;
                StringBuilder text = new StringBuilder();
                while (numRead < numOfChars) {
                    String line = br.readLine();
                    if (line == null) {
                        // End of file, so break out of loop
                        break;
                    }
                    if (line.startsWith("# ")) { // Begins a new Note
                        // If we just finished a note,
                        if (timestamp != 0) {
                            // Pack previous note into a Note object.
                            Note prev = new Note(timestamp, text.toString().trim());
                            notes.add(prev); // Add it to the list
                        }
                        text.delete(0, text.length()); // Empty the StringBuilder
                        // and parse the timeString into a timestamp (long)
                        String timeString = line.substring(2, line.length());
                        try {
                            timestamp = DateFormat.getDateTimeInstance().parse(timeString).getTime();
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
            return notes;
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

        public void setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            mTimeString = getDateFormat().format(new Date(mTimestamp));
        }

        // Returns the string that describes when this note was created or edited
        @Override
        public String toString() {
            return mTimeString;
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
    }

    private static final String ARGSKEY_NOTES_FILENAME = "NotesViewFragment.notesFilename";

    public static NotesViewFragment createNew(Scripture scripture) {
        NotesViewFragment f = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putString(ARGSKEY_NOTES_FILENAME, scripture.getFilename());
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_notes_view, container, false);
        ListView lv = (ListView) v.findViewById(R.id.notes_list);

        // Get the pointer to the notes file.
        String filename = getArguments().getString(ARGSKEY_NOTES_FILENAME);
        File file = new File(getContext().getDir(Scripture.NOTES_DIR, Context.MODE_PRIVATE),
                filename);

        // Load notes from the file
        List<Note> notes = Note.loadFromFile(file);
        if (notes.size() != 0) {
            // If there are some notes that already exist, populate the ListView with them
            ListAdapter adapter = new ArrayAdapter<Note>(getContext(), R.layout.list_item,
                    R.id.listitem_text, notes);
            lv.setAdapter(adapter);
            // TODO: set up onClickListener to view note
        } else {
            // If no notes exist for this scripture, put an "Add note" entry in the ListView
            ListAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.list_item,
                    R.id.listitem_text, new String[]{"Tap to add a note"});
            lv.setAdapter(adapter);
            // TODO: set up onClickListener to add note
        }

        return v;
    }
}

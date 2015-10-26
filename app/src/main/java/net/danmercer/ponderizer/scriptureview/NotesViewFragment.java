package net.danmercer.ponderizer.scriptureview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dan on 10/24/2015.
 */
public class NotesViewFragment extends Fragment {
    private static final int REQUEST_ADD_NOTE = 2;
    private static final int REQUEST_EDIT_NOTE = 3;

    private static final String ARGSKEY_NOTES_FILENAME = "NotesViewFragment.notesFilename";

    public static NotesViewFragment createNew(Scripture scripture) {
        NotesViewFragment f = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putString(ARGSKEY_NOTES_FILENAME, scripture.getFilename());
        f.setArguments(args);
        return f;
    }

    private File mFile; // The file where these notes are stored.
    private ListView mListView; // The ListView that displays the list of notes
    private List<Note> mNotes; // The list of Notes
    private ArrayAdapter<Note> mAdapter;
    private ListAdapter mEmptyAdapter;
    private Note mNoteBeingEdited;

    public File getFile() {
        return mFile;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_notes_view, container, false);
        mListView = (ListView) v.findViewById(R.id.notes_list);

        // Get the pointer to the notes file.
        String filename = getArguments().getString(ARGSKEY_NOTES_FILENAME);
        mFile = new File(getContext().getDir(Scripture.NOTES_DIR, Context.MODE_PRIVATE),
                filename);

        // Load notes into list
        mNotes = new LinkedList<>();
        Note.loadFromFile(mFile, mNotes);
        mAdapter = new ArrayAdapter<Note>(getContext(), R.layout.list_item,
                R.id.listitem_text, mNotes);
        mEmptyAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item,
                R.id.listitem_text, new String[]{"Tap to add a note"});
        refreshNoteList();

        return v;
    }

    private void refreshNoteList() {
        if (mNotes.size() != 0) {
            // If there are some notes that already exist, populate the ListView with them

            // We constructed the adapter with an empty mNotes list, so now we need to tell it that
            // the list changed.
            mAdapter.notifyDataSetChanged();

            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    launchEditNoteActivity(position);
                }
            });
            registerForContextMenu(mListView);
        } else {
            // If no notes exist for this scripture, put an "Add note" entry in the ListView
            mListView.setAdapter(mEmptyAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    launchAddNoteActivity();
                }
            });
            unregisterForContextMenu(mListView);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_note, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position; // Get index of item that was long-clicked

        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteNoteAt(pos);
                return true;
            case R.id.action_view:
                launchEditNoteActivity(pos);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteNoteAt(int position) {
        deleteNote(mNotes.get(position));
    }

    private void deleteNote(Note n) {
        mNotes.remove(n);
        // Write modified notes list to file
        Note.writeNotesToFile(mNotes, mFile);

        // this method switches the ListView over to a the mEmptyAdapter if necessary
        refreshNoteList();
    }

    public void launchEditNoteActivity(int pos) {
        // Launch activity to edit the note
        Intent i = new Intent(getContext(), AddNoteActivity.class);
        mNoteBeingEdited = mNotes.get(pos);
        i.putExtra(Note.EXTRA_NOTE_TIME, mNoteBeingEdited.getTimestamp());
        i.putExtra(Note.EXTRA_NOTE_TEXT, mNoteBeingEdited.getText());
        startActivityForResult(i, REQUEST_EDIT_NOTE);
    }

    public void launchAddNoteActivity() {
        // Launch an activity to add a new note
        Intent i = new Intent(getContext(), AddNoteActivity.class);
        startActivityForResult(i, REQUEST_ADD_NOTE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the activity was successful
        if (resultCode == Activity.RESULT_OK) {
            // If it was a request to add a note
            if (requestCode == REQUEST_ADD_NOTE) {
                String noteText = data.getStringExtra(Note.EXTRA_NOTE_TEXT);
                long time = data.getLongExtra(Note.EXTRA_NOTE_TIME, 0);
                mNotes.add(0, new Note(time, noteText));
                Note.writeNotesToFile(mNotes, mFile);
                refreshNoteList();
            } else if (requestCode == REQUEST_EDIT_NOTE) {
                mNotes.remove(mNoteBeingEdited);
                String noteText = data.getStringExtra(Note.EXTRA_NOTE_TEXT);
                long time = data.getLongExtra(Note.EXTRA_NOTE_TIME, 0);
                mNotes.add(0, new Note(time, noteText));
                Note.writeNotesToFile(mNotes, mFile);
                refreshNoteList();
            }
        } else if (resultCode == AddNoteActivity.RESULT_DELETED && requestCode == REQUEST_EDIT_NOTE) {
            // Delete the note that was being edited
            deleteNote(mNoteBeingEdited);
        }
        mNoteBeingEdited = null;
    }
}

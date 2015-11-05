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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedList;

public class ScriptureListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_CATEGORY = "category";
    public static final int REQUEST_VIEW_SCRIPTURE = 1;

    private LinkedList<Scripture> mScriptureList;
    private View mContentView;
    private ListView mListView;
    private NewMainActivity.Category mCategory;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ScriptureListFragment newInstance(NewMainActivity.Category category) {
        ScriptureListFragment fragment = new ScriptureListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public ScriptureListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = (NewMainActivity.Category) getArguments().getSerializable(ARG_CATEGORY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.content_main_onelist, container, false);
        mListView = (ListView) mContentView.findViewById(R.id.scripturesList);

        refreshScriptureList();
        return mContentView;
    }

    // Fills or refreshes the list of Scriptures
    private void refreshScriptureList() {
        mScriptureList = Scripture.loadScriptures(getContext(), mCategory);

        if (!mScriptureList.isEmpty()) {
            mListView.setAdapter(new ArrayAdapter<Scripture>(getContext(), R.layout.list_item, R.id.listitem_text, mScriptureList));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Scripture scripture = mScriptureList.get(position);
                    startActivityForResult(scripture.getViewIntent(getContext()), REQUEST_VIEW_SCRIPTURE);
                }
            });
            registerForContextMenu(mListView);
        } else if (mCategory == NewMainActivity.Category.IN_PROGRESS){
            // Set up the "Tap to Add a Scripture" filler list entry.
            mListView.setAdapter(new ArrayAdapter<String>(
                    getContext(),
                    R.layout.list_item,
                    R.id.listitem_text,
                    new String[]{getResources().getString(R.string.tap_to_add_scripture)}));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Launch the AddScriptureInstructions activity
                    Intent i = new Intent(getContext(), AddScriptureInstructions.class);
                    startActivity(i);
                }
            });
            unregisterForContextMenu(mListView);
        } else {
            // Set up a "Memorize a scripture to put it here." filler entry.
            mListView.setAdapter(new ArrayAdapter<String>(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    new String[]{getResources().getString(R.string.complete_a_scripture)}));
            mListView.setOnItemClickListener(null);
            unregisterForContextMenu(mListView);
        }
    }

    // This is called to create the context menu for the list menu items.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_scripture, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // Called when the user taps an item in a scripture context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position; // Get index of item that was long-clicked
        Scripture s = mScriptureList.get(pos);

        switch (item.getItemId()) {

            case R.id.action_delete:
                // Delete this scripture
                s.deleteWithConfirmation(getContext(), new Runnable() {
                    @Override
                    public void run() {
                        // Refresh mScripturesList
                        refreshScriptureList();
                    }
                });
                return true;

            case R.id.action_add_note:
                startActivity(s.getAddNoteIntent(getContext()));
                return true;

            case R.id.action_view:
                startActivityForResult(s.getViewIntent(getContext()), REQUEST_VIEW_SCRIPTURE);
                return true;

            case R.id.action_memorize:
                startActivity(s.getMemorizeIntent(getContext()));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
}

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

package net.danmercer.ponderizer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import net.danmercer.ponderizer.NewMainActivity;
import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;
import net.danmercer.ponderizer.memorize.MemorizeActivity;
import net.danmercer.ponderizer.scriptureview.AddNoteActivity;
import net.danmercer.ponderizer.scriptureview.ScriptureIntent;
import net.danmercer.ponderizer.scriptureview.ScriptureViewActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ScriptureAppWidgetConfigureActivity ScriptureAppWidgetConfigureActivity}
 */
public class ScriptureAppWidget extends AppWidgetProvider {

    // CONSTANTS:
    static final String PREFS_NAME = "net.danmercer.ponderizer.ScriptureAppWidget";
    static final String PREF_KEY_REFERENCE = "ref_";
    static final String PREF_KEY_TEXT = "body_";
    private static final String PREF_KEY_CATEGORY = "category_";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    // Called by onUpdate() for each widget to be updated.
    // Also called by ScriptureAppWidgetConfigureActivity.onListItemClick()
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Get scripture info from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String reference = prefs.getString(PREF_KEY_REFERENCE + appWidgetId, null);
        String scriptureText = prefs.getString(PREF_KEY_TEXT + appWidgetId, null);
        String categoryName = prefs.getString(PREF_KEY_CATEGORY + appWidgetId,
                NewMainActivity.Category.IN_PROGRESS.name());
        NewMainActivity.Category cat = NewMainActivity.Category.valueOf(categoryName);

        boolean hasScripture = reference != null && scriptureText != null;
        if (!hasScripture) {
            return;
        }

        Scripture s = new Scripture(reference, scriptureText, cat);
        boolean scriptureFileExists = s.fileExists(context);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scripture_app_widget);

        // Put text into views in widget
        views.setTextViewText(R.id.widget_header, reference);
        views.setTextViewText(R.id.widget_body, scriptureText);

        // Set up memorize PendingIntent
        Intent memIntent = new ScriptureIntent(context, MemorizeActivity.class, s);
        PendingIntent memorizeIntent = PendingIntent.getActivity(context, appWidgetId,
                memIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up add note PendingIntent
        Intent noteIntent = new ScriptureIntent(context, AddNoteActivity.class, s);
        PendingIntent addNoteIntent = PendingIntent.getActivity(context, appWidgetId,
                noteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up scripture view PendingIntent
        Intent viewIntent = new ScriptureIntent(context, ScriptureViewActivity.class, s);
        PendingIntent scripViewIntent = PendingIntent.getActivity(context, appWidgetId,
                viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (scriptureFileExists) {
            // Attach proper PendingIntent to Memorize button
            views.setOnClickPendingIntent(R.id.action_memorize, memorizeIntent);

            // Attach proper PendingIntent to Add Note button
            views.setOnClickPendingIntent(R.id.action_add_note, addNoteIntent);

            // Attach proper PendingIntent to Scripture text (takes user to ScriptureViewActivity
            // when clicked)
            views.setOnClickPendingIntent(R.id.widget_body, scripViewIntent);
        } else {
            // Hide buttons, because the scripture is no longer saved
            // In the user's list.
            views.setViewVisibility(R.id.action_memorize, View.GONE);
            views.setViewVisibility(R.id.action_add_note, View.GONE);

            // Cancel all PendingIntents
            memorizeIntent.cancel();
            addNoteIntent.cancel();
            scripViewIntent.cancel();
        }

        // Set up widget "context menu"
        Intent menuIntent = new ScriptureIntent(context, WidgetPopupMenuActivity.class, s);
        menuIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent menuPending = PendingIntent.getActivity(context, appWidgetId, menuIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_header, menuPending);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Called by ScriptureAppWidgetConfigureActivity to save widget info to a SharedPreferences
    public static void saveWidgetPrefs(Context context, int appWidgetId, String reference, String text, NewMainActivity.Category cat) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_KEY_REFERENCE + appWidgetId, reference);
        prefs.putString(PREF_KEY_TEXT + appWidgetId, text);
        prefs.putString(PREF_KEY_CATEGORY + appWidgetId, cat.name());
        prefs.commit();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int id : appWidgetIds) {
            // Remove widget info from SharedPreferences
            SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
            prefs.remove(PREF_KEY_REFERENCE + id);
            prefs.remove(PREF_KEY_TEXT + id);
            prefs.commit();
        }
    }

    // Can be called with a Context to update all the appwidgets of this type
    public static void updateAllAppWidgets(Context c) {
        ComponentName componentName = new ComponentName(
                c.getApplicationContext(),
                ScriptureAppWidget.class);
        int[] ids = AppWidgetManager.getInstance(c).getAppWidgetIds(componentName);

        Intent i = new Intent(c, ScriptureAppWidget.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        c.sendBroadcast(i);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


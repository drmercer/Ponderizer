package net.danmercer.ponderizer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import net.danmercer.ponderizer.memorize.MemorizeActivity;
import net.danmercer.ponderizer.scriptureview.AddNoteActivity;
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
        // Get widget text from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String reference = prefs.getString(PREF_KEY_REFERENCE + appWidgetId, null);
        String scriptureText = prefs.getString(PREF_KEY_TEXT + appWidgetId, null);
        boolean hasScripture = reference != null && scriptureText != null;
        if (!hasScripture) {
            return;
        }

        Scripture s = new Scripture(reference, scriptureText);
        boolean scriptureFileExists = s.fileExists(context);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scripture_app_widget);

        // Put text into views in widget
        views.setTextViewText(R.id.widget_header, reference);
        views.setTextViewText(R.id.widget_body, scriptureText);

        // Set up memorize PendingIntent
        Intent memIntent = new Intent(context, MemorizeActivity.class);
        memIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
        PendingIntent memorizeIntent = PendingIntent.getActivity(context, appWidgetId,
                memIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up add note PendingIntent
        Intent noteIntent = new Intent(context, AddNoteActivity.class);
        noteIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
        PendingIntent addNoteIntent = PendingIntent.getActivity(context, appWidgetId,
                noteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up scripture view PendingIntent
        Intent viewIntent = new Intent(context, ScriptureViewActivity.class);
        viewIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
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
            views.setOnClickPendingIntent(R.id.widget_header, scripViewIntent);
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

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Called by ScriptureAppWidgetConfigureActivity to save widget info to a SharedPreferences
    public static void saveWidgetPrefs(Context context, int appWidgetId, String reference, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_KEY_REFERENCE + appWidgetId, reference);
        prefs.putString(PREF_KEY_TEXT + appWidgetId, text);
        prefs.commit();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            // Remove widget info from SharedPreferences
            SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
            prefs.remove(PREF_KEY_REFERENCE + appWidgetIds[i]);
            prefs.remove(PREF_KEY_TEXT + appWidgetIds[i]);
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


package net.danmercer.ponderizer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scripture_app_widget);

        // Put text into views in widget
        views.setTextViewText(R.id.widget_header, reference);
        views.setTextViewText(R.id.widget_body, scriptureText);

        // Set up Memorize button
        Intent memIntent = new Intent(context, MemorizeActivity.class);
        memIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
        PendingIntent memorizeIntent = PendingIntent.getActivity(context, appWidgetId, memIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.action_memorize, memorizeIntent);

        // Set up Add Note button
        Intent noteIntent = new Intent(context, AddNoteActivity.class);
        noteIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
        PendingIntent addNoteIntent = PendingIntent.getActivity(context, appWidgetId, noteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.action_add_note, addNoteIntent);

        // Set up Scripture text (takes user to ScriptureViewActivity when clicked)
        Intent viewIntent = new Intent(context, ScriptureViewActivity.class);
        viewIntent.putExtra(Scripture.EXTRA_SCRIPTURE, s);
        PendingIntent scripViewIntent = PendingIntent.getActivity(context, appWidgetId, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_body, scripViewIntent);
        views.setOnClickPendingIntent(R.id.widget_header, scripViewIntent);

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

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


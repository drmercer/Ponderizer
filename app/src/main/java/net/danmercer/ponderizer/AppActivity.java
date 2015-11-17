package net.danmercer.ponderizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.danmercer.ponderizer.settings.ReminderPreference;
import net.danmercer.ponderizer.settings.ReminderReceiver;
import net.danmercer.ponderizer.settings.SettingsActivity;

/**
 * Superclass to be extended by major activities in the app. This allows all superclasses to have
 * these actions built into the App Bar:
 * "Settings"
 * "Help"
 * "Send Feedback"
 * "About"
 *
 * Created by Dan on 11/16/2015.
 */
public abstract class AppActivity extends AppCompatActivity {
    private static final String KEY_VERSION = "APP_VERSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int prefsVersion = prefs.getInt(KEY_VERSION, 0);
        if (prefsVersion < BuildConfig.VERSION_CODE) { // This is true after an install or update
            // Set up default preferences for prefs that haven't been set
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);

            if (prefs.getBoolean(getString(R.string.prefkey_weekly_reminder), false)) {
                // Set weekly reminder to be on if it should be
                String key = getString(R.string.prefkey_weekly_reminder_time);
                String setting = prefs.getString(key,
                        ReminderPreference.DEFAULT_VALUE);
                ReminderPreference.Util u = new ReminderPreference.Util(this, key);
                u.parseValue(setting);
                u.setupAlarm();
            }

            prefs.edit().putInt(KEY_VERSION, BuildConfig.VERSION_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_universal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_feedback:
                launchFeedbackDialog();
                return true;

            case R.id.action_help:
                Uri url = Uri.parse("https://github.com/drmercer/Ponderizer/wiki/User-Help-Pages");
                Intent help = new Intent(Intent.ACTION_VIEW, url);
                startActivity(help);
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchFeedbackDialog() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setMessage(R.string.feedback_dialog_message);
        AlertDialog.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("onclick", "fired");
                launchFeedbackEmailIntent();
            }
        };
        db.setPositiveButton(R.string.feedback_dialog_confirm, l);
        db.setNegativeButton(R.string.feedback_dialog_cancel, null);
        db.show();
    }

    // Called by the feedback dialog to start an activity via an email intent.
    private void launchFeedbackEmailIntent() {
        String version = null;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Never going to happen.
            e.printStackTrace();
        }
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"danmercerdev@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT,
                "Feedback for Ponderizer (" + version + ")");
        i.putExtra(Intent.EXTRA_TEXT, "Please describe the problem you are having or the feature " +
                "you would like to see. Thanks for supporting the Ponderizer app!\n\n");
        startActivity(i);
    }
}

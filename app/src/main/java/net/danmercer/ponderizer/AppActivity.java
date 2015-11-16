package net.danmercer.ponderizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

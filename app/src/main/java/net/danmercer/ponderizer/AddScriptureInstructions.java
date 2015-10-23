package net.danmercer.ponderizer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AddScriptureInstructions extends AppCompatActivity {

    public static final String GOSPEL_LIBRARY_PACKAGE_NAME = "org.lds.ldssa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scripture_instructions);

        Button b = (Button) findViewById(R.id.instruction_button);
        if (isGospelLibraryInstalled()) {
            b.setText("Launch Gospel Library app");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // End this activity
                    // Launch Gospel Library app
                    Intent i = getPackageManager().getLaunchIntentForPackage(GOSPEL_LIBRARY_PACKAGE_NAME);
                    startActivity(i);
                }
            });
        } else {
            b.setText("Install Gopsel Library app from Google Play");
            // Open Google play to install the app.
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    installGospelLibrary();
                }
            });
        }

        ImageView demo = (ImageView) findViewById(R.id.how_to_add_view);
        AnimationDrawable ad = (AnimationDrawable) demo.getDrawable();
        ad.start();
    }

    /**
     * Launches the Play store (or a browser as fallback) to install the Gospel Library app.
     */
    private void installGospelLibrary() {
        finish();
        try{
            // Try to open using the Play app directly
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "market://details?id=" + GOSPEL_LIBRARY_PACKAGE_NAME)));
        } catch (ActivityNotFoundException e) {
            // Fallback: use Play store URL
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id="
                            + GOSPEL_LIBRARY_PACKAGE_NAME)));
        }
    }

    private boolean isGospelLibraryInstalled() {
        PackageManager pm = getPackageManager();
        try{
            pm.getPackageInfo(GOSPEL_LIBRARY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

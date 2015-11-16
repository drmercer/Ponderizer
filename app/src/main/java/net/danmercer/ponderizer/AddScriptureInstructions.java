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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AddScriptureInstructions extends AppActivity {

    public static final String GOSPEL_LIBRARY_PACKAGE_NAME = "org.lds.ldssa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scripture_instructions);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up Gospel Library button
        Button b = (Button) findViewById(R.id.button_open_gospel_library);
        if (isGospelLibraryInstalled()) {
            b.setText(R.string.open_gospel_library);
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
            b.setText(R.string.install_gospel_library);
            // Open Google play to install the app.
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    installGospelLibrary();
                }
            });
        }

        // Set up Import SM button
        Button buttonSM = (Button) findViewById(R.id.button_import_sm);
        buttonSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScriptureMasteryHelper.showDialog(AddScriptureInstructions.this, new Runnable() {
                    @Override
                    public void run() {
                        finish(); // Finish the activity after the user imports SM scriptures
                    }
                });
            }
        });

        // Start tutorial animation
        ImageView demo = (ImageView) findViewById(R.id.how_to_add_view);
        AnimationDrawable ad = (AnimationDrawable) demo.getDrawable();
        ad.start();
    }

    /**
     * Launches the Play store (or a browser as fallback) to install the Gospel Library app.
     */
    private void installGospelLibrary() {
        finish();
        try {
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
        try {
            pm.getPackageInfo(GOSPEL_LIBRARY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

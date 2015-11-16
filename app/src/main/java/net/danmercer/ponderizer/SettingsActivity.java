package net.danmercer.ponderizer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
    public static class SettingsFragment extends PreferenceFragment {

        public static SettingsFragment getInstance() {
            // This method is here in case we need to instantiate the fragment with arguments, in
            // which case we should avoid using anything but the default constructor.
            return new SettingsFragment();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            // Set up "Import from any app" setting.
            findPreference("import_any_app").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PackageManager pm = getActivity().getPackageManager();
                    int flags = PackageManager.DONT_KILL_APP;
                    String packageName = BuildConfig.APPLICATION_ID;
                    ComponentName cnameGLOnly = new ComponentName(packageName, packageName + ".AddScriptureTextOnlyGospelLibrary");
                    ComponentName cnameAnyApp = new ComponentName(packageName, packageName + ".AddScriptureTextActivityFromAnyApp");
                    if (newValue.equals(Boolean.TRUE)) { // Allow import from any app
                        pm.setComponentEnabledSetting(cnameGLOnly, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, flags);
                        pm.setComponentEnabledSetting(cnameAnyApp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, flags);
                    } else {// Only allow import from Gospel Library
                        pm.setComponentEnabledSetting(cnameGLOnly, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, flags);
                        pm.setComponentEnabledSetting(cnameAnyApp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, flags);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .add(R.id.frag_container, SettingsFragment.getInstance())
                .commit();
    }
}

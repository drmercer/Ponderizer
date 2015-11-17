package net.danmercer.ponderizer.settings;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import net.danmercer.ponderizer.BuildConfig;
import net.danmercer.ponderizer.R;

public class SettingsActivity extends AppCompatActivity {
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            // Grab Preference objects
            final Preference wklyRemEnabledPref = findPreference(getString(R.string.prefkey_weekly_reminder));
            final ReminderPreference wklyRemPref =
                    (ReminderPreference) findPreference(getString(R.string.prefkey_weekly_reminder_time));

            // Set up notification settings
            findPreference(getString(R.string.prefkey_allow_notifs))
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if (Boolean.FALSE.equals(newValue)) {
                                // Turn off and disable "Weekly Reminder" pref
                                ((TwoStatePreference) wklyRemEnabledPref).setChecked(false);
                                // Manually call the listener
                                wklyRemEnabledPref.getOnPreferenceChangeListener()
                                        .onPreferenceChange(wklyRemEnabledPref, false);
                                // (Disabling is done by the system because the "Weekly Reminder"
                                // preference depends on the "Notifications" preference)
                            }
                            return true;
                        }
                    });
            wklyRemEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Boolean.TRUE.equals(newValue)) {
                        wklyRemPref.enableAlarm();
                    } else {
                        wklyRemPref.disableAlarm();
                    }
                    return true;
                }
            });

            // Set up "Import from any app" setting.
            findPreference(getString(R.string.prefkey_import_any_app))
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            PackageManager pm = getActivity().getPackageManager();
                            final int flags = PackageManager.DONT_KILL_APP;
                            final String packageName = BuildConfig.APPLICATION_ID;

                            // The component names of the two activitiy-alias components
                            final ComponentName cnameGLOnly = new ComponentName(packageName,
                                    packageName + ".AddScriptureTextOnlyGospelLibrary");
                            final ComponentName cnameAnyApp = new ComponentName(packageName,
                                    packageName + ".AddScriptureTextActivityFromAnyApp");
                            Log.d("SettingsActivity", "Preference value: " + newValue);

                            // Flags to enable or disable components
                            final int stateDisabled = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                            final int stateEnabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

                            if (newValue.equals(Boolean.TRUE)) { // Allow import from any app
                                pm.setComponentEnabledSetting(cnameGLOnly, stateDisabled, flags);
                                pm.setComponentEnabledSetting(cnameAnyApp, stateEnabled, flags);
                                Log.d("SettingsActivity", "Other-app import enabled");
                            } else {// Only allow import from Gospel Library
                                pm.setComponentEnabledSetting(cnameGLOnly, stateEnabled, flags);
                                pm.setComponentEnabledSetting(cnameAnyApp, stateDisabled, flags);
                                Log.d("SettingsActivity", "Other-app import disabled");
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
                .replace(R.id.frag_container, new SettingsFragment())
                .commit();
    }
}

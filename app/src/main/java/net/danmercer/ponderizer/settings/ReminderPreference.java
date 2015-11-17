package net.danmercer.ponderizer.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TimePicker;

import net.danmercer.ponderizer.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Dan on 11/16/2015.
 */
public class ReminderPreference extends DialogPreference {
    private static final String DEFAULT_VALUE = "Sunday/17:00";

    private static final DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("cccc");
    private Spinner mSpinner;
    private TimePicker mPicker;
    private String mValue;

    private static int getWeekday(String text) {
        try {
            Date date = WEEKDAY_FORMATTER.parse(text);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            Log.e("ReminderPreference", "Weekday format error", e);
            return 0;
        }
    }

    private static String getWeekdayString(int weekday) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, weekday);
        Date date = cal.getTime();
        return WEEKDAY_FORMATTER.format(date);
    }

    public ReminderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pref_reminder);
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue = getPersistedString(DEFAULT_VALUE);
        } else {
            mValue = (String) defaultValue;
        }
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        mPicker = (TimePicker) view.findViewById(R.id.time_picker);
        mSpinner = (Spinner) view.findViewById(R.id.weekday_spinner);
        setCurrentValue(mValue);
        return view;
    }

    private void setCurrentValue(String value) {
        Log.d("ReminderPreference", "persistedString = " + value);
        int weekday, hour, minute;
        try{
            // Get weekday and hour from string
            String[] parts = value.split("[/:]");
            weekday = getWeekday(parts[0]);
            hour = Integer.parseInt(parts[1]);
            minute = Integer.parseInt(parts[2]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Log.e("ReminderPreference", "Error parsing value string.", e);
            return;
        }

        mSpinner.setSelection(weekday - 1); // Calendar.<WEEKDAY> constants are 1-indexed
        mPicker.setCurrentHour(hour);
        mPicker.setCurrentMinute(minute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Store the setting (via persistString() )
            int weekday = mSpinner.getSelectedItemPosition() + 1;
            // These are deprecated, but the new ones are API 23 only
            int hour = mPicker.getCurrentHour();
            int minute = mPicker.getCurrentMinute();
            String wkdayString = getWeekdayString(weekday);
            mValue = String.format("%s/%d:%d", wkdayString, hour, minute);
            persistString(mValue);

            // Set up the recurring alarm
            setupAlarm(weekday, hour, minute);
        }
    }

    public void disableAlarm() {
        // Get the PendingIntent that the alarm should fire, and cancel it in the AlarmManager
        PendingIntent intent = getPendingIntent();
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        am.cancel(intent);
    }

    // Called by Settings
    public void enableAlarm() {
        mValue = getPersistedString(DEFAULT_VALUE);
        setCurrentValue(mValue);
        int weekday = mSpinner.getSelectedItemPosition() + 1;
        // These are deprecated, but the new ones are API 23 only
        int hour = mPicker.getCurrentHour();
        int minute = mPicker.getCurrentMinute();
        setupAlarm(weekday, hour, minute);
    }

    private PendingIntent getPendingIntent() {
        Intent broadcastIntent = new Intent(getContext(), ReminderReceiver.class);
        String key = getKey();
        broadcastIntent.putExtra(ReminderReceiver.EXTRA_KEY, key);
        return PendingIntent.getBroadcast(getContext(), key.hashCode(),
                broadcastIntent, 0);
    }

    private void setupAlarm(int weekday, int hour, int minute) {
        // Get the PendingIntent that the alarm should fire
        PendingIntent intent = getPendingIntent();

        // Set up the alarm
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.DAY_OF_WEEK, weekday);
        if (cal.before(new Date())) { // If it's before now, add 7 days so it won't instantly fire
            cal.add(Calendar.DAY_OF_MONTH, 7);
        }
        long triggerTime = cal.getTimeInMillis();
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY * 7, intent);
        Log.i("ReminderReceiver", "ReminderReceiver fired!");
    }

}

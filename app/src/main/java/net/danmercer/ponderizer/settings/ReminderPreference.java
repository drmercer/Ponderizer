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
    public static final String DEFAULT_VALUE = "Sunday/17:00";

    public static class Util {
        private static final SimpleDateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("cccc");
        private final Context mContext;
        private final String mKey;
        private int mWeekday;
        private int mHour;
        private int mMinute;

        public Util(Context context, String key) {
            this.mContext = context;
            this.mKey = key;
        }

        public void parseValue(String value) {
            try{
                // Get weekday and hour from string
                String[] parts = value.split("[/:]");
                mWeekday = Util.getWeekday(parts[0]);
                mHour = Integer.parseInt(parts[1]);
                mMinute = Integer.parseInt(parts[2]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                Log.e("ReminderPreference", "Error parsing value string.", e);
            }
        }

        public int getWeekday() {
            return mWeekday;
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        public void setupAlarm() {
            // Get the PendingIntent that the alarm should fire
            PendingIntent intent = getPendingIntent(mContext, mKey);

            // Set up the alarm
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, mHour);
            cal.set(Calendar.MINUTE, mMinute);
            cal.set(Calendar.DAY_OF_WEEK, mWeekday);
            cal.set(Calendar.SECOND, 0);

            // If the time is past, add 7 days so it won't instantly fire
            long triggerTime = cal.getTimeInMillis();
            long currentTime = System.currentTimeMillis();
            while (triggerTime < currentTime) {
                triggerTime += 604800000; // 7 days in milliseconds
            }

            // DEBUG
            DateFormat df = DateFormat.getDateTimeInstance();
            Log.d("Util", df.format(new Date(triggerTime)));


            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY * 7, intent);
        }

        public static int getWeekday(String text) {
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

        private static PendingIntent getPendingIntent(Context c, String key) {
            Intent broadcastIntent = new Intent(c, ReminderReceiver.class);
            broadcastIntent.putExtra(ReminderReceiver.EXTRA_KEY, key);
            return PendingIntent.getBroadcast(c, key.hashCode(), broadcastIntent, 0);
        }
    }
    private Spinner mSpinner;
    private TimePicker mPicker;
    private String mValue;

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

        // Initialize views with value
        Util u = new Util(getContext(), getKey());
        u.parseValue(mValue);
        mSpinner.setSelection(u.getWeekday() - 1); // Calendar.<WEEKDAY> constants are 1-indexed
        mPicker.setCurrentHour(u.getHour());
        mPicker.setCurrentMinute(u.getMinute());
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Store the setting (via persistString() )
            int weekday = mSpinner.getSelectedItemPosition() + 1;
            // These are deprecated, but the new ones are API 23 only
            int hour = mPicker.getCurrentHour();
            int minute = mPicker.getCurrentMinute();
            String wkdayString = Util.getWeekdayString(weekday);
            mValue = String.format("%s/%d:%d", wkdayString, hour, minute);
            persistString(mValue);

            // Set up the recurring alarm
            Util u = new Util(getContext(), getKey());
            u.parseValue(mValue);
            u.setupAlarm();
        }
    }

    public void disableAlarm() {
        // Get the PendingIntent that the alarm should fire, and cancel it in the AlarmManager
        PendingIntent intent = Util.getPendingIntent(getContext(), getKey());
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        am.cancel(intent);
    }

    // Called by Settings
    public void enableAlarm() {
        mValue = getPersistedString(DEFAULT_VALUE);
        Util u = new Util(getContext(), getKey());
        u.parseValue(mValue);
        u.setupAlarm();
    }

}

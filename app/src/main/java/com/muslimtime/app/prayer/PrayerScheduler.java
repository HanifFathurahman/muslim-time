package com.muslimtime.app.prayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.muslimtime.app.Preferences;
import com.muslimtime.app.notify.AdhanReceiver;

import java.util.Calendar;
import java.util.List;

public final class PrayerScheduler {
    public static final String ACTION_ADHAN = "com.muslimtime.app.action.ADHAN";
    public static final String EXTRA_PRAYER_NAME = "prayer_name";
    public static final String EXTRA_PRAYER_TIME = "prayer_time";
    public static final String EXTRA_REMINDER_MODE = "reminder_mode";

    private PrayerScheduler() {
    }

    public static void scheduleNextAlarms(Context context) {
        if (!Preferences.isAdhanEnabled(context)) {
            cancelAlarms(context);
            return;
        }

        cancelAlarms(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        long now = System.currentTimeMillis();
        double latitude = Preferences.getLatitude(context);
        double longitude = Preferences.getLongitude(context);
        String method = Preferences.getMethod(context);

        Calendar day = Calendar.getInstance();
        for (int offset = 0; offset < 2; offset++) {
            Calendar targetDay = (Calendar) day.clone();
            targetDay.add(Calendar.DAY_OF_YEAR, offset);
            List<PrayerTime> times = PrayerTimesCalculator.calculate(targetDay, latitude, longitude, method);

            for (int i = 0; i < times.size(); i++) {
                PrayerTime prayerTime = times.get(i);
                if (!prayerTime.notify
                        || !Preferences.isPrayerReminderActive(context, prayerTime.name)
                        || prayerTime.timeMillis <= now + 30_000L) {
                    continue;
                }
                schedule(context, alarmManager, targetDay, i, prayerTime);
            }
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar day = Calendar.getInstance();
        for (int offset = -1; offset <= 2; offset++) {
            Calendar targetDay = (Calendar) day.clone();
            targetDay.add(Calendar.DAY_OF_YEAR, offset);
            for (int i = 0; i < 6; i++) {
                alarmManager.cancel(pendingIntent(context, targetDay, i, null));
            }
        }
    }

    private static void schedule(
            Context context,
            AlarmManager alarmManager,
            Calendar day,
            int index,
            PrayerTime prayerTime
    ) {
        Intent intent = new Intent(context, AdhanReceiver.class);
        intent.setAction(ACTION_ADHAN);
        intent.putExtra(EXTRA_PRAYER_NAME, prayerTime.name);
        intent.putExtra(EXTRA_PRAYER_TIME, prayerTime.label());
        intent.putExtra(EXTRA_REMINDER_MODE, Preferences.getPrayerMode(context, prayerTime.name));
        PendingIntent pendingIntent = pendingIntent(context, day, index, intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTime.timeMillis, pendingIntent);
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTime.timeMillis, pendingIntent);
        }
    }

    private static PendingIntent pendingIntent(Context context, Calendar day, int index, Intent sourceIntent) {
        Intent intent = sourceIntent;
        if (intent == null) {
            intent = new Intent(context, AdhanReceiver.class);
            intent.setAction(ACTION_ADHAN);
        }
        int requestCode = requestCode(day, index);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    private static int requestCode(Calendar day, int index) {
        int year = day.get(Calendar.YEAR) % 100;
        int dayOfYear = day.get(Calendar.DAY_OF_YEAR);
        return 20_000 + (year * 4_000) + (dayOfYear * 10) + index;
    }
}

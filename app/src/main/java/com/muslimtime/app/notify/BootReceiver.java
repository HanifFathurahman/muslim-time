package com.muslimtime.app.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.muslimtime.app.Preferences;
import com.muslimtime.app.location.SignificantLocationMonitor;
import com.muslimtime.app.prayer.PrayerScheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                && !Intent.ACTION_DATE_CHANGED.equals(action)) {
            return;
        }
        PrayerScheduler.scheduleNextAlarms(context);
        if (!Preferences.isManualLocation(context)) {
            SignificantLocationMonitor.register(context);
        }
    }
}

package com.muslimtime.app.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.muslimtime.app.Preferences;
import com.muslimtime.app.prayer.PrayerScheduler;

@SuppressWarnings("deprecation")
public class SignificantLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !SignificantLocationMonitor.ACTION_LOCATION_CHANGED.equals(intent.getAction())) {
            return;
        }
        Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (location == null || Preferences.isManualLocation(context)) {
            return;
        }

        PendingResult pendingResult = goAsync();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleSignificantLocation(context.getApplicationContext(), location);
                } finally {
                    pendingResult.finish();
                }
            }
        }).start();
    }

    private void handleSignificantLocation(Context context, Location location) {
        if (Preferences.hasLocation(context)) {
            float distance = LocationHelper.distanceMeters(
                    Preferences.getLatitude(context),
                    Preferences.getLongitude(context),
                    location.getLatitude(),
                    location.getLongitude()
            );
            if (distance < SignificantLocationMonitor.MIN_DISTANCE_M) {
                return;
            }
        }

        LocationHelper.LocationName name = LocationHelper.resolve(context, location.getLatitude(), location.getLongitude());
        String oldCity = Preferences.getLocationCity(context);
        if (Preferences.hasLocation(context) && LocationHelper.sameCity(oldCity, name.city)) {
            return;
        }

        Preferences.saveLocation(context, location.getLatitude(), location.getLongitude(), name.label, name.city, false);
        if (Preferences.isAdhanEnabled(context)) {
            PrayerScheduler.scheduleNextAlarms(context);
        }
        SignificantLocationMonitor.register(context);
    }
}

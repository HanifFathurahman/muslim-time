package com.muslimtime.app.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

public final class SignificantLocationMonitor {
    public static final String ACTION_LOCATION_CHANGED = "com.muslimtime.app.action.SIGNIFICANT_LOCATION_CHANGED";
    static final float MIN_DISTANCE_M = 7_000f;
    private static final long MIN_TIME_MS = 60 * 60 * 1000L;
    private static final int REQUEST_CODE = 730;

    private SignificantLocationMonitor() {
    }

    @SuppressLint("MissingPermission")
    public static void register(Context context) {
        if (!hasLocationPermission(context)) {
            return;
        }
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null || !manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return;
        }
        try {
            PendingIntent pendingIntent = pendingIntent(context);
            manager.removeUpdates(pendingIntent);
            manager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    pendingIntent
            );
        } catch (SecurityException ignored) {
        }
    }

    public static void unregister(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.removeUpdates(pendingIntent(context));
    }

    private static boolean hasLocationPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, SignificantLocationReceiver.class);
        intent.setAction(ACTION_LOCATION_CHANGED);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, pendingFlags());
    }

    private static int pendingFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        return flags;
    }
}

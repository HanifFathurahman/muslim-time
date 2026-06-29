package com.muslimtime.app.notify;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.muslimtime.app.MainActivity;
import com.muslimtime.app.Preferences;
import com.muslimtime.app.R;
import com.muslimtime.app.prayer.PrayerScheduler;

@SuppressWarnings("deprecation")
public class AdhanReceiver extends android.content.BroadcastReceiver {
    private static final String CHANNEL_PREFIX = "prayer_reminder_channel_v3_";

    @Override
    public void onReceive(Context context, Intent intent) {
        String prayer = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_NAME);
        String time = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_TIME);
        if (prayer == null) {
            prayer = "Shalat";
        }
        if (time == null) {
            time = "";
        }
        String mode = intent.getStringExtra(PrayerScheduler.EXTRA_REMINDER_MODE);
        if (mode == null) {
            mode = Preferences.getPrayerMode(context, prayer);
        }
        if (Preferences.PRAYER_MODE_OFF.equals(mode)) {
            PrayerScheduler.scheduleNextAlarms(context);
            return;
        }

        showNotification(context, prayer, time);
        if (Preferences.PRAYER_MODE_ADHAN.equals(mode)) {
            startPlayback(context, prayer);
        }
        PrayerScheduler.scheduleNextAlarms(context);
    }

    private void showNotification(Context context, String prayer, String time) {
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        String tone = Preferences.getTone(context);
        String channelId = CHANNEL_PREFIX + tone;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Pengingat shalat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi waktu shalat Muslim Time");
            channel.enableVibration(true);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }

        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                44,
                launchIntent,
                pendingFlags()
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, channelId)
                : new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.ic_stat_muslim_time)
                .setContentTitle("Waktu " + prayer)
                .setContentText(time.isEmpty() ? "Saatnya shalat " + prayer : "Pukul " + time + " - saatnya shalat " + prayer)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        manager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    private void startPlayback(Context context, String prayer) {
        String tone = Preferences.getTone(context);
        if (Preferences.TONE_SILENT.equals(tone)) {
            return;
        }
        Intent intent = new Intent(context, AdhanPlaybackService.class);
        intent.setAction(AdhanPlaybackService.ACTION_PLAY);
        intent.putExtra("tone", tone);
        intent.putExtra(PrayerScheduler.EXTRA_PRAYER_NAME, prayer);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception ignored) {
        }
    }

    public static String toneLabel(String tone) {
        if (Preferences.TONE_ADHAN_CLASSIC.equals(tone)) {
            return "Madinah";
        }
        if (Preferences.TONE_SYSTEM.equals(tone)) {
            return "Default";
        }
        if (Preferences.TONE_SILENT.equals(tone)) {
            return "hening";
        }
        return "Default";
    }

    private static int pendingFlags() {
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }
}

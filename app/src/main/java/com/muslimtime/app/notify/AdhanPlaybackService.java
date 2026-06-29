package com.muslimtime.app.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import com.muslimtime.app.MainActivity;
import com.muslimtime.app.Preferences;
import com.muslimtime.app.R;
import com.muslimtime.app.prayer.PrayerScheduler;

@SuppressWarnings("deprecation")
public class AdhanPlaybackService extends Service {
    public static final String ACTION_PLAY = "com.muslimtime.app.action.PLAY_ADHAN";
    public static final String ACTION_STOP = "com.muslimtime.app.action.STOP_ADHAN";
    private static final String CHANNEL_ID = "adhan_playback_v2";
    private static final int NOTIFICATION_ID = 710;
    private static final long MAX_PLAYBACK_MS = 4 * 60 * 1000L;

    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock wakeLock;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String tone = intent == null ? Preferences.getTone(this) : intent.getStringExtra("tone");
        String prayer = intent == null ? "Shalat" : intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_NAME);
        if (tone == null) {
            tone = Preferences.getTone(this);
        }
        if (prayer == null) {
            prayer = "Shalat";
        }

        startForeground(NOTIFICATION_ID, playbackNotification(prayer));
        playTone(tone);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(stopRunnable);
        stopPlayback();
        releaseWakeLock();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playTone(String tone) {
        if (Preferences.TONE_SILENT.equals(tone)) {
            stopSelf();
            return;
        }

        stopPlayback();
        acquireWakeLock();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            int resId = Preferences.TONE_ADHAN_CLASSIC.equals(tone)
                    ? R.raw.adhan_madinah
                    : R.raw.adhan_default;
            AssetFileDescriptor afd = getResources().openRawResourceFd(resId);
            if (afd == null) {
                stopSelf();
                return;
            }
            try {
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } finally {
                afd.close();
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSelf();
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
            handler.postDelayed(stopRunnable, MAX_PLAYBACK_MS);
        } catch (Exception exception) {
            stopSelf();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (IllegalStateException ignored) {
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void acquireWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            return;
        }
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return;
        }
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MuslimTime:AdhanPlayback");
        wakeLock.acquire(MAX_PLAYBACK_MS + 10_000L);
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;
    }

    private Notification playbackNotification(String prayer) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pemutar adzan",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }

        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 711, launchIntent, pendingFlags());

        Intent stopIntent = new Intent(this, AdhanPlaybackService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 712, stopIntent, pendingFlags());

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_muslim_time)
                .setContentTitle("Adzan " + prayer)
                .setContentText("Sedang memutar nada adzan")
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .addAction(0, "Berhenti", stopPendingIntent);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        return builder.build();
    }

    private static int pendingFlags() {
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }
}

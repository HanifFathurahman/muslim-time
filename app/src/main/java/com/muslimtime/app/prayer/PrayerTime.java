package com.muslimtime.app.prayer;

import java.util.Locale;

public class PrayerTime {
    public final String name;
    public final int hour;
    public final int minute;
    public final long timeMillis;
    public final boolean notify;

    public PrayerTime(String name, int hour, int minute, long timeMillis, boolean notify) {
        this.name = name;
        this.hour = hour;
        this.minute = minute;
        this.timeMillis = timeMillis;
        this.notify = notify;
    }

    public String label() {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }
}

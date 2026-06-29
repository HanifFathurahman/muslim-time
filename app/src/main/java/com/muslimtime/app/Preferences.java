package com.muslimtime.app;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
    public static final String TONE_ADHAN_SOFT = "adhan_makkah";
    public static final String TONE_ADHAN_CLASSIC = "adhan_madinah";
    public static final String TONE_SYSTEM = "system_alarm";
    public static final String TONE_SILENT = "silent";
    public static final String PRAYER_MODE_ADHAN = "adhan";
    public static final String PRAYER_MODE_NOTIFY = "notify";
    public static final String PRAYER_MODE_OFF = "off";

    public static final String METHOD_KEMENAG = "kemenag";
    public static final String METHOD_MWL = "mwl";
    public static final String METHOD_UMM_AL_QURA = "umm_al_qura";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final String PREFS = "muslim_time_prefs";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_CITY = "location_city";
    private static final String KEY_LOCATION_TIME = "location_time";
    private static final String KEY_HAS_LOCATION = "has_location";
    private static final String KEY_MANUAL_LOCATION = "manual_location";
    private static final String KEY_LOCATION_PERMISSION_ASKED = "location_permission_asked";
    private static final String KEY_ADHAN_ENABLED = "adhan_enabled";
    private static final String KEY_TONE = "tone";
    private static final String KEY_METHOD = "method";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_LEGACY_THEME = "quran_theme";

    public static final double DEFAULT_LAT = -6.2088;
    public static final double DEFAULT_LON = 106.8456;

    private Preferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean hasLocation(Context context) {
        return prefs(context).getBoolean(KEY_HAS_LOCATION, false);
    }

    public static double getLatitude(Context context) {
        return Double.longBitsToDouble(prefs(context).getLong(KEY_LAT, Double.doubleToLongBits(DEFAULT_LAT)));
    }

    public static double getLongitude(Context context) {
        return Double.longBitsToDouble(prefs(context).getLong(KEY_LON, Double.doubleToLongBits(DEFAULT_LON)));
    }

    public static String getLocationName(Context context) {
        return prefs(context).getString(KEY_LOCATION_NAME, "Jakarta (contoh)");
    }

    public static String getLocationCity(Context context) {
        return prefs(context).getString(KEY_LOCATION_CITY, getLocationName(context));
    }

    public static long getLocationTime(Context context) {
        return prefs(context).getLong(KEY_LOCATION_TIME, 0L);
    }

    public static void saveLocation(Context context, double lat, double lon, String label) {
        saveLocation(context, lat, lon, label, label, false);
    }

    public static void saveLocation(Context context, double lat, double lon, String label, String city, boolean manual) {
        prefs(context).edit()
                .putBoolean(KEY_HAS_LOCATION, true)
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LON, Double.doubleToRawLongBits(lon))
                .putString(KEY_LOCATION_NAME, label)
                .putString(KEY_LOCATION_CITY, city == null || city.trim().isEmpty() ? label : city)
                .putLong(KEY_LOCATION_TIME, System.currentTimeMillis())
                .putBoolean(KEY_MANUAL_LOCATION, manual)
                .apply();
    }

    public static boolean isManualLocation(Context context) {
        return prefs(context).getBoolean(KEY_MANUAL_LOCATION, false);
    }

    public static boolean wasLocationPermissionAsked(Context context) {
        return prefs(context).getBoolean(KEY_LOCATION_PERMISSION_ASKED, false);
    }

    public static void setLocationPermissionAsked(Context context) {
        prefs(context).edit().putBoolean(KEY_LOCATION_PERMISSION_ASKED, true).apply();
    }

    public static boolean isAdhanEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ADHAN_ENABLED, false);
    }

    public static void setAdhanEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ADHAN_ENABLED, enabled).apply();
    }

    public static String getTone(Context context) {
        String tone = prefs(context).getString(KEY_TONE, TONE_ADHAN_SOFT);
        if (TONE_SYSTEM.equals(tone)) {
            return TONE_ADHAN_SOFT;
        }
        return tone;
    }

    public static void setTone(Context context, String tone) {
        if (TONE_SYSTEM.equals(tone)) {
            tone = TONE_ADHAN_SOFT;
        }
        prefs(context).edit().putString(KEY_TONE, tone).apply();
    }

    public static String getMethod(Context context) {
        return prefs(context).getString(KEY_METHOD, METHOD_KEMENAG);
    }

    public static void setMethod(Context context, String method) {
        prefs(context).edit().putString(KEY_METHOD, method).apply();
    }

    public static String getTheme(Context context) {
        return prefs(context).getString(KEY_THEME, prefs(context).getString(KEY_LEGACY_THEME, THEME_LIGHT));
    }

    public static void setTheme(Context context, String theme) {
        prefs(context).edit().putString(KEY_THEME, theme).apply();
    }

    public static boolean isDarkTheme(Context context) {
        return THEME_DARK.equals(getTheme(context));
    }

    public static String getPrayerMode(Context context, String prayerName) {
        if (prayerName == null || "Terbit".equals(prayerName)) {
            return PRAYER_MODE_OFF;
        }
        return prefs(context).getString(prayerModeKey(prayerName), PRAYER_MODE_ADHAN);
    }

    public static void setPrayerMode(Context context, String prayerName, String mode) {
        prefs(context).edit().putString(prayerModeKey(prayerName), mode).apply();
    }

    public static boolean isPrayerReminderActive(Context context, String prayerName) {
        return !PRAYER_MODE_OFF.equals(getPrayerMode(context, prayerName));
    }

    public static boolean isPrayerAdhan(Context context, String prayerName) {
        return PRAYER_MODE_ADHAN.equals(getPrayerMode(context, prayerName));
    }

    private static String prayerModeKey(String prayerName) {
        return "prayer_mode_" + prayerName.toLowerCase().replace(" ", "_");
    }
}

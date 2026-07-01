package com.muslimtime.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.muslimtime.app.location.LocationHelper;
import com.muslimtime.app.location.SignificantLocationMonitor;
import com.muslimtime.app.notify.AdhanReceiver;
import com.muslimtime.app.notify.AdhanPlaybackService;
import com.muslimtime.app.prayer.PrayerScheduler;
import com.muslimtime.app.prayer.PrayerTime;
import com.muslimtime.app.prayer.PrayerTimesCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
@SuppressLint("SetTextI18n")
public class MainActivity extends Activity implements SensorEventListener {
    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_SCHEDULE = 1;
    private static final int SCREEN_HIJRI_CALENDAR = 2;
    private static final int SCREEN_ID_CALENDAR = 3;
    private static final int SCREEN_QIBLA = 4;
    private static final int SCREEN_SETTINGS = 5;
    private static final int SCREEN_RAMADAN = 6;
    private static final int REQ_LOCATION = 60;
    private static final int REQ_NOTIFICATION = 61;
    private static final double KAABA_LAT = 21.4225;
    private static final double KAABA_LON = 39.8262;
    private static final long FRESH_LOCATION_MS = 30 * 60 * 1000L;
    private static final int IMSAK_OFFSET_MINUTES = 10;
    private static final int SAHUR_WINDOW_MINUTES = 90;

    private static final String[] PRAYERS = {"Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"};
    private static final String[] HIJRI_MONTHS = {
            "", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir",
            "Rajab", "Syaban", "Ramadan", "Syawal", "Zulkaidah", "Zulhijah"
    };
    private static final String[] WEEKDAYS = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private static final HijriEvent[] HIJRI_EVENTS = {
            new HijriEvent(1, 1, "Tahun Baru Hijriah"),
            new HijriEvent(1, 10, "Asyura"),
            new HijriEvent(3, 12, "Maulid Nabi"),
            new HijriEvent(7, 27, "Isra Miraj"),
            new HijriEvent(9, 1, "Awal Ramadan"),
            new HijriEvent(9, 17, "Nuzulul Quran"),
            new HijriEvent(10, 1, "Idul Fitri"),
            new HijriEvent(12, 8, "Awal Tarwiyah"),
            new HijriEvent(12, 9, "Arafah"),
            new HijriEvent(12, 10, "Idul Adha")
    };
    private static final HolidayEvent[] ID_HOLIDAYS_2026 = {
            new HolidayEvent(2026, 1, 1, "Tahun Baru Masehi", false),
            new HolidayEvent(2026, 1, 16, "Isra Mikraj Nabi Muhammad", false),
            new HolidayEvent(2026, 2, 16, "Cuti Bersama Imlek", true),
            new HolidayEvent(2026, 2, 17, "Tahun Baru Imlek", false),
            new HolidayEvent(2026, 3, 18, "Cuti Bersama Nyepi", true),
            new HolidayEvent(2026, 3, 19, "Hari Suci Nyepi", false),
            new HolidayEvent(2026, 3, 20, "Cuti Bersama Idul Fitri", true),
            new HolidayEvent(2026, 3, 21, "Idul Fitri", false),
            new HolidayEvent(2026, 3, 22, "Idul Fitri", false),
            new HolidayEvent(2026, 3, 23, "Cuti Bersama Idul Fitri", true),
            new HolidayEvent(2026, 3, 24, "Cuti Bersama Idul Fitri", true),
            new HolidayEvent(2026, 4, 3, "Wafat Yesus Kristus", false),
            new HolidayEvent(2026, 4, 5, "Paskah", false),
            new HolidayEvent(2026, 5, 1, "Hari Buruh", false),
            new HolidayEvent(2026, 5, 14, "Kenaikan Yesus Kristus", false),
            new HolidayEvent(2026, 5, 15, "Cuti Bersama Kenaikan Yesus", true),
            new HolidayEvent(2026, 5, 27, "Idul Adha", false),
            new HolidayEvent(2026, 5, 28, "Cuti Bersama Idul Adha", true),
            new HolidayEvent(2026, 5, 31, "Waisak", false),
            new HolidayEvent(2026, 6, 1, "Hari Lahir Pancasila", false),
            new HolidayEvent(2026, 6, 16, "Tahun Baru Islam", false),
            new HolidayEvent(2026, 8, 17, "Hari Kemerdekaan RI", false),
            new HolidayEvent(2026, 8, 25, "Maulid Nabi Muhammad", false),
            new HolidayEvent(2026, 12, 24, "Cuti Bersama Natal", true),
            new HolidayEvent(2026, 12, 25, "Hari Natal", false)
    };

    private final Locale indonesia = new Locale("id", "ID");
    private FrameLayout rootFrame;
    private ImageView shellBackground;
    private View shellWash;
    private FrameLayout contentFrame;
    private ScrollView currentScrollView;
    private int currentScreen = SCREEN_HOME;
    private int displayedHijriYear;
    private int displayedHijriMonth;
    private int displayedGregorianYear;
    private int displayedGregorianMonth;

    private TextView clockText;
    private TextView countdownText;
    private TextView countdownLabelText;
    private long nextPrayerMillis;
    private String nextPrayerName;
    private TextView ramadanCountdownText;
    private TextView ramadanCountdownLabelText;
    private long ramadanTargetMillis;
    private String ramadanTargetName;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClockText();
            clockHandler.postDelayed(this, 1000);
        }
    };

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private final float[] gravityValues = new float[3];
    private final float[] magneticValues = new float[3];
    private boolean hasGravity;
    private boolean hasMagnetic;
    private TextView qiblaNeedle;
    private TextView qiblaStatus;
    private double currentQiblaBearing;
    private AlertDialog exitDialog;

    private static final class NextPrayerInfo {
        final PrayerTime prayerTime;
        final boolean tomorrow;

        NextPrayerInfo(PrayerTime prayerTime, boolean tomorrow) {
            this.prayerTime = prayerTime;
            this.tomorrow = tomorrow;
        }
    }

    private static final class RamadanInfo {
        final HijriDate hijriDate;
        final boolean inRamadan;
        final int daysToRamadan;
        final Calendar sahurStart;
        final Calendar imsak;
        final Calendar subuh;
        final Calendar buka;
        final Calendar isya;
        final Calendar target;
        final String targetName;

        RamadanInfo(HijriDate hijriDate, boolean inRamadan, int daysToRamadan,
                    Calendar sahurStart, Calendar imsak, Calendar subuh,
                    Calendar buka, Calendar isya, Calendar target, String targetName) {
            this.hijriDate = hijriDate;
            this.inRamadan = inRamadan;
            this.daysToRamadan = daysToRamadan;
            this.sahurStart = sahurStart;
            this.imsak = imsak;
            this.subuh = subuh;
            this.buka = buka;
            this.isya = isya;
            this.target = target;
            this.targetName = targetName;
        }
    }

    private static final class HijriEvent {
        final int month;
        final int day;
        final String name;

        HijriEvent(int month, int day, String name) {
            this.month = month;
            this.day = day;
            this.name = name;
        }
    }

    private static final class HolidayEvent {
        final int year;
        final int month;
        final int day;
        final String name;
        final boolean jointLeave;

        HolidayEvent(int year, int month, int day, String name, boolean jointLeave) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.name = name;
            this.jointLeave = jointLeave;
        }
    }

    private static final class HijriDate {
        final int year;
        final int month;
        final int day;

        HijriDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        static HijriDate fromGregorian(Calendar calendar) {
            int jd = gregorianToJulian(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            int year = (int) Math.floor((30.0 * (jd - 1948439) + 10646.0) / 10631.0);
            int month = (int) Math.ceil((jd - 29 - islamicToJulian(year, 1, 1)) / 29.5) + 1;
            month = Math.max(1, Math.min(12, month));
            int day = jd - islamicToJulian(year, month, 1) + 1;
            if (day <= 0) {
                month--;
                if (month <= 0) {
                    month = 12;
                    year--;
                }
                day = jd - islamicToJulian(year, month, 1) + 1;
            }
            return new HijriDate(year, month, day);
        }

        static Calendar toGregorian(int year, int month, int day) {
            return julianToGregorian(islamicToJulian(year, month, day));
        }

        static int monthLength(int year, int month) {
            int nextYear = month == 12 ? year + 1 : year;
            int nextMonth = month == 12 ? 1 : month + 1;
            return islamicToJulian(nextYear, nextMonth, 1) - islamicToJulian(year, month, 1);
        }

        private static int islamicToJulian(int year, int month, int day) {
            return day
                    + (int) Math.ceil(29.5 * (month - 1))
                    + (year - 1) * 354
                    + (int) Math.floor((3 + 11 * year) / 30.0)
                    + 1948439;
        }

        private static int gregorianToJulian(int year, int month, int day) {
            int a = (14 - month) / 12;
            int y = year + 4800 - a;
            int m = month + 12 * a - 3;
            return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        }

        private static Calendar julianToGregorian(int jd) {
            int a = jd + 32044;
            int b = (4 * a + 3) / 146097;
            int c = a - (146097 * b) / 4;
            int d = (4 * c + 3) / 1461;
            int e = c - (1461 * d) / 4;
            int m = (5 * e + 2) / 153;
            int day = e - (153 * m + 2) / 5 + 1;
            int month = m + 3 - 12 * (m / 10);
            int year = 100 * b + d - 4800 + (m / 10);
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(year, month - 1, day);
            return calendar;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySystemBars();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        buildShell();
        installBackHandler();
        renderHome();
        requestInitialLocationIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Preferences.isAdhanEnabled(this)) {
            PrayerScheduler.scheduleNextAlarms(this);
        }
        if (currentScreen == SCREEN_HOME) {
            renderHome();
        } else if (currentScreen == SCREEN_SCHEDULE) {
            renderSchedule();
        } else if (currentScreen == SCREEN_HIJRI_CALENDAR) {
            renderHijriCalendar();
        } else if (currentScreen == SCREEN_ID_CALENDAR) {
            renderIndonesiaCalendar();
        } else if (currentScreen == SCREEN_QIBLA) {
            renderQibla();
        } else if (currentScreen == SCREEN_SETTINGS) {
            renderSettings();
        } else if (currentScreen == SCREEN_RAMADAN) {
            renderRamadan();
        }
        registerSignificantLocationMonitor();
        registerCompass();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockRunnable);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, gravityValues.length);
            hasGravity = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, magneticValues.length);
            hasMagnetic = true;
        }

        if (!hasGravity || !hasMagnetic) {
            return;
        }

        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)) {
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) {
                azimuth += 360f;
            }
            updateQiblaNeedle(azimuth);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void buildShell() {
        rootFrame = new FrameLayout(this);

        shellBackground = new ImageView(this);
        shellBackground.setImageResource(R.drawable.bg_muslim_time);
        shellBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rootFrame.addView(shellBackground, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        shellWash = new View(this);
        rootFrame.addView(shellWash, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        contentFrame = new FrameLayout(this);
        rootFrame.addView(contentFrame, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        applyShellTheme();
        setContentView(rootFrame);
    }

    private void installBackHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    new android.window.OnBackInvokedCallback() {
                        @Override
                        public void onBackInvoked() {
                            if (!handleBackNavigation()) {
                                confirmExitApp();
                            }
                        }
                    }
            );
        }
    }

    private LinearLayout beginPage() {
        applyShellTheme();
        applySystemBars();
        contentFrame.removeAllViews();
        clockHandler.removeCallbacks(clockRunnable);
        clockText = null;
        countdownText = null;
        countdownLabelText = null;
        nextPrayerMillis = 0L;
        nextPrayerName = null;
        ramadanCountdownText = null;
        ramadanCountdownLabelText = null;
        ramadanTargetMillis = 0L;
        ramadanTargetName = null;
        qiblaNeedle = null;
        qiblaStatus = null;

        currentScrollView = new ScrollView(this);
        currentScrollView.setFillViewport(false);
        currentScrollView.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(18), dp(16), dp(22));
        currentScrollView.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        contentFrame.addView(currentScrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return page;
    }

    private void renderHome() {
        currentScreen = SCREEN_HOME;
        LinearLayout page = beginPage();
        Calendar now = Calendar.getInstance();
        HijriDate hijriDate = HijriDate.fromGregorian(now);
        List<PrayerTime> todayTimes = todayPrayerTimes();
        NextPrayerInfo next = nextPrayer(todayTimes);

        addHomeHero(page, hijriDate, next);
        addSpacer(page, 12);
        if (hijriDate.month == 9) {
            addRamadanDashboardCard(page, ramadanInfo(now));
            addSpacer(page, 12);
        }
        addQuickActions(page);
        addSpacer(page, 12);
        addTodayPrayerStrip(page, todayTimes);
        addSpacer(page, 12);
        addLocationCard(page);
        addSpacer(page, 12);
        addQiblaCard(page);
        registerCompass();
    }

    private void renderSchedule() {
        currentScreen = SCREEN_SCHEDULE;
        LinearLayout page = beginPage();
        addTopBar(page, "Jadwal Shalat");

        List<PrayerTime> times = todayPrayerTimes();
        addNextPrayerCard(page, nextPrayer(times));
        addSpacer(page, 12);

        LinearLayout listCard = card(Color.WHITE);
        listCard.addView(text("Hari ini", 18, color("#172026"), Typeface.BOLD));
        listCard.addView(text(gregorianLabel(Calendar.getInstance()), 13, color("#4B635E"), Typeface.NORMAL));
        addSpacer(listCard, 8);
        for (PrayerTime prayerTime : times) {
            listCard.addView(prayerRow(prayerTime));
        }
        page.addView(listCard);
        addSpacer(page, 12);

        addAdhanControlCard(page);
        addSpacer(page, 12);
        addLocationCard(page);
    }

    private void renderRamadan() {
        currentScreen = SCREEN_RAMADAN;
        LinearLayout page = beginPage();
        addTopBar(page, "Mode Ramadhan");

        RamadanInfo info = ramadanInfo(Calendar.getInstance());
        addRamadanHero(page, info);
        addSpacer(page, 12);
        addRamadanScheduleCard(page, info);
        addSpacer(page, 12);
        addRamadanTipsCard(page, info);
    }

    private void renderHijriCalendar() {
        currentScreen = SCREEN_HIJRI_CALENDAR;
        LinearLayout page = beginPage();
        addTopBar(page, "Kalender Islam");

        HijriDate today = HijriDate.fromGregorian(Calendar.getInstance());
        if (displayedHijriYear == 0 || displayedHijriMonth == 0) {
            displayedHijriYear = today.year;
            displayedHijriMonth = today.month;
        }

        addCalendarHero(page, today);
        addSpacer(page, 12);
        addMonthNavigator(page);
        addSpacer(page, 8);
        addMonthGrid(page, today);
        addSpacer(page, 12);
        addUpcomingEvents(page);
    }

    private void renderIndonesiaCalendar() {
        currentScreen = SCREEN_ID_CALENDAR;
        LinearLayout page = beginPage();
        addTopBar(page, "Kalender Indonesia");

        Calendar today = Calendar.getInstance();
        if (displayedGregorianYear == 0 || displayedGregorianMonth == 0) {
            displayedGregorianYear = today.get(Calendar.YEAR);
            displayedGregorianMonth = today.get(Calendar.MONTH) + 1;
        }

        addIndonesiaCalendarHero(page, today);
        addSpacer(page, 12);
        addGregorianMonthNavigator(page);
        addSpacer(page, 8);
        addGregorianMonthGrid(page, today);
        addSpacer(page, 12);
        addHolidayList(page);
    }

    private void renderQibla() {
        currentScreen = SCREEN_QIBLA;
        LinearLayout page = beginPage();
        addTopBar(page, "Arah Kiblat");
        addQiblaHero(page);
        addSpacer(page, 12);
        addLocationCard(page);
        registerCompass();
    }

    private void renderSettings() {
        currentScreen = SCREEN_SETTINGS;
        LinearLayout page = beginPage();
        addTopBar(page, "Pengaturan");

        LinearLayout toneCard = card(Color.WHITE);
        toneCard.addView(text("Nada adzan", 18, color("#172026"), Typeface.BOLD));
        toneCard.addView(text("Dipakai saat mode shalat = Adzan.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(toneCard, 8);
        toneCard.addView(toneGroup());
        addSpacer(toneCard, 8);
        toneCard.addView(secondaryButton("Coba nada sekarang", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTestPrayerReminder(true);
            }
        }));
        page.addView(toneCard);
        addSpacer(page, 12);

        LinearLayout prayerModeCard = card(Color.WHITE);
        prayerModeCard.addView(text("Pengingat tiap shalat", 18, color("#172026"), Typeface.BOLD));
        prayerModeCard.addView(text("Adzan, notif biasa, atau mati.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(prayerModeCard, 8);
        addPrayerModeRows(prayerModeCard);
        page.addView(prayerModeCard);
        addSpacer(page, 12);

        LinearLayout methodCard = card(Color.WHITE);
        methodCard.addView(text("Metode waktu shalat", 18, color("#172026"), Typeface.BOLD));
        methodCard.addView(text("Default Kemenag Indonesia.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(methodCard, 8);
        methodCard.addView(methodGroup());
        page.addView(methodCard);
        addSpacer(page, 12);

        LinearLayout themeCard = card(Color.WHITE);
        themeCard.addView(text("Tema aplikasi", 18, color("#172026"), Typeface.BOLD));
        addSpacer(themeCard, 8);
        themeCard.addView(themeGroup());
        page.addView(themeCard);
        addSpacer(page, 12);

        LinearLayout permissionCard = card(Color.WHITE);
        permissionCard.addView(text("Izin aplikasi", 18, color("#172026"), Typeface.BOLD));
        permissionCard.addView(text("Lokasi, notifikasi, dan jadwal tepat.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(permissionCard, 8);
        permissionCard.addView(primaryButton(hasLocationPermission() ? "Perbarui lokasi" : "Izinkan lokasi GPS", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationPermissionAndFetch();
            }
        }));
        addSpacer(permissionCard, 8);
        permissionCard.addView(secondaryButton("Izinkan notifikasi", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ensureNotificationPermission();
            }
        }));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addSpacer(permissionCard, 8);
            permissionCard.addView(secondaryButton("Buka izin jadwal tepat", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askExactAlarmIfNeeded();
                }
            }));
        }
        page.addView(permissionCard);
    }

    private void addHomeHero(LinearLayout page, HijriDate hijriDate, NextPrayerInfo next) {
        LinearLayout hero = gradientCard(color("#0F766E"), color("#14532D"));
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Muslim Time", 26, Color.WHITE, Typeface.BOLD);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        clockText = text("--:--:--", 18, Color.WHITE, Typeface.BOLD);
        clockText.setGravity(Gravity.END);
        top.addView(clockText);
        hero.addView(top, fullWidthParams());

        Calendar today = Calendar.getInstance();
        hero.addView(text(gregorianLabel(today), 15, color("#DFFBF4"), Typeface.BOLD));
        TextView hijri = text(hijriDate.day + " " + HIJRI_MONTHS[hijriDate.month] + " " + hijriDate.year + " H",
                15, color("#D9F99D"), Typeface.BOLD);
        hero.addView(hijri);
        HolidayEvent holiday = holidayFor(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        if (holiday != null) {
            hero.addView(text(holiday.name, 13, color("#FDE68A"), Typeface.BOLD));
        }
        addSpacer(hero, 16);

        TextView label = text("Shalat berikutnya", 14, color("#DFFBF4"), Typeface.NORMAL);
        hero.addView(label);

        TextView prayerName = text(next == null || next.prayerTime == null ? "-" : next.prayerTime.name,
                38, Color.WHITE, Typeface.BOLD);
        hero.addView(prayerName);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        TextView time = text(next == null || next.prayerTime == null ? "--:--" : next.prayerTime.label() + (next.tomorrow ? " besok" : ""),
                24, color("#FDE68A"), Typeface.BOLD);
        bottom.addView(time, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        countdownText = text("--:--:--", 18, Color.WHITE, Typeface.BOLD);
        countdownText.setGravity(Gravity.END);
        bottom.addView(countdownText);
        hero.addView(bottom, fullWidthParams());

        countdownLabelText = text("Menuju waktu shalat", 13, color("#CFF7EC"), Typeface.NORMAL);
        countdownLabelText.setGravity(Gravity.END);
        hero.addView(countdownLabelText, fullWidthParams());

        if (next != null && next.prayerTime != null) {
            nextPrayerMillis = next.prayerTime.timeMillis;
            nextPrayerName = next.prayerTime.name;
        }
        page.addView(hero);
        clockHandler.post(clockRunnable);
    }

    private void addRamadanDashboardCard(LinearLayout page, RamadanInfo info) {
        LinearLayout card = gradientCard(color("#8A6D1D"), color("#0F766E"));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setClickable(true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderRamadan();
            }
        });
        card.addView(text("Mode Ramadhan", 14, color("#FDE68A"), Typeface.BOLD));
        card.addView(text("Buka " + timeLabel(info.buka), 28, Color.WHITE, Typeface.BOLD));
        ramadanCountdownText = text("--:--:--", 18, Color.WHITE, Typeface.BOLD);
        card.addView(ramadanCountdownText);
        ramadanCountdownLabelText = text("Menuju buka puasa", 13, color("#DFFBF4"), Typeface.NORMAL);
        card.addView(ramadanCountdownLabelText);
        setRamadanTarget(info);
        page.addView(card);
        clockHandler.post(clockRunnable);
    }

    private void addRamadanHero(LinearLayout page, RamadanInfo info) {
        LinearLayout hero = gradientCard(color("#0F766E"), color("#14532D"));
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        hero.addView(text(info.inRamadan ? "Ramadhan hari ke-" + info.hijriDate.day : "Mode Ramadhan",
                15, color("#D9F99D"), Typeface.BOLD));
        hero.addView(text(info.inRamadan ? "Buka " + timeLabel(info.buka) : nextRamadanText(info),
                32, Color.WHITE, Typeface.BOLD));
        hero.addView(text(gregorianLabel(Calendar.getInstance()), 14, color("#DFFBF4"), Typeface.NORMAL));
        addSpacer(hero, 12);

        if (info.inRamadan) {
            ramadanCountdownText = text("--:--:--", 28, color("#FDE68A"), Typeface.BOLD);
            hero.addView(ramadanCountdownText);
            ramadanCountdownLabelText = text("Menuju waktu Ramadhan", 14, Color.WHITE, Typeface.BOLD);
            hero.addView(ramadanCountdownLabelText);
            setRamadanTarget(info);
            clockHandler.post(clockRunnable);
        } else {
            hero.addView(text("Jadwal puasa tetap bisa dipratinjau.", 15, Color.WHITE, Typeface.BOLD));
        }
        page.addView(hero);
    }

    private void addRamadanScheduleCard(LinearLayout page, RamadanInfo info) {
        LinearLayout card = card(Color.WHITE);
        card.addView(text("Jadwal puasa", 18, color("#172026"), Typeface.BOLD));
        card.addView(text(Preferences.getLocationName(this), 13, color("#4B635E"), Typeface.NORMAL));
        addSpacer(card, 10);
        card.addView(ramadanTimeRow("Sahur", timeLabel(info.sahurStart) + " - " + timeLabel(info.imsak),
                "Disarankan selesai sebelum imsak"));
        card.addView(ramadanTimeRow("Imsak", timeLabel(info.imsak),
                IMSAK_OFFSET_MINUTES + " menit sebelum Subuh"));
        card.addView(ramadanTimeRow("Subuh", timeLabel(info.subuh), "Mulai waktu shalat Subuh"));
        card.addView(ramadanTimeRow("Buka", timeLabel(info.buka), "Maghrib"));
        card.addView(ramadanTimeRow("Isya", timeLabel(info.isya), "Persiapan tarawih"));
        page.addView(card);
    }

    private void addRamadanTipsCard(LinearLayout page, RamadanInfo info) {
        LinearLayout card = card(Color.WHITE);
        card.addView(text(info.inRamadan ? "Hari ini" : "Catatan", 18, color("#172026"), Typeface.BOLD));
        String note = info.inRamadan
                ? "Countdown otomatis mengikuti waktu sahur, imsak, dan buka."
                : "Mode ini aktif otomatis saat bulan Ramadhan. Lokasi tetap hemat baterai.";
        card.addView(text(note, 14, color("#4B635E"), Typeface.NORMAL));
        page.addView(card);
    }

    private View ramadanTimeRow(String title, String time, String note) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setBackground(rounded(color("#E8F3EF"), 8, color("#D8E7E2")));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(text(title, 16, color("#172026"), Typeface.BOLD));
        labels.addView(text(note, 12, color("#4B635E"), Typeface.NORMAL));
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView timeView = text(time, 18, color("#0F766E"), Typeface.BOLD);
        timeView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(timeView);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row, fullWidthParams());
        addSpacer(wrapper, 8);
        return wrapper;
    }

    private void addTodayPrayerStrip(LinearLayout page, List<PrayerTime> times) {
        LinearLayout card = card(Color.WHITE);
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(text("Ringkasan hari ini", 18, color("#172026"), Typeface.BOLD),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(text(methodLabel(Preferences.getMethod(this)), 12, color("#0F766E"), Typeface.BOLD));
        card.addView(header);
        addSpacer(card, 10);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.HORIZONTAL);
        for (PrayerTime time : times) {
            if (!time.notify) {
                continue;
            }
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(8), dp(4), dp(8));
            item.setBackground(rounded(color("#E8F3EF"), 8));
            item.addView(text(shortPrayerName(time.name), 12, color("#4B635E"), Typeface.BOLD));
            item.addView(text(time.label(), 16, color("#172026"), Typeface.BOLD));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(dp(2), 0, dp(2), 0);
            grid.addView(item, params);
        }
        card.addView(grid);
        page.addView(card);
    }

    private void addQuickActions(LinearLayout page) {
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(actionButton("Jadwal", "Waktu shalat", R.drawable.ic_menu_clock, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderSchedule();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalGap(row1, 10);
        row1.addView(actionButton("Kiblat", "Arah shalat", R.drawable.ic_menu_compass, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderQibla();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actions.addView(row1);

        addSpacer(actions, 10);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(actionButton("Kalender Islam", "Hijriah", R.drawable.ic_menu_calendar, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderHijriCalendar();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalGap(row2, 10);
        row2.addView(actionButton("Kalender Indonesia", "Libur nasional", R.drawable.ic_menu_calendar, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderIndonesiaCalendar();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actions.addView(row2);

        addSpacer(actions, 10);
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.addView(actionButton("Ramadhan", "Imsak & buka", R.drawable.ic_menu_ramadan, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderRamadan();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalGap(row3, 10);
        row3.addView(actionButton("Pengaturan", "Adzan, lokasi", R.drawable.ic_menu_settings, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderSettings();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actions.addView(row3);
        page.addView(actions);
    }

    private void addCalendarPreviews(LinearLayout page, HijriDate today) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(calendarPreviewCard("Kalender Islam",
                today.day + " " + HIJRI_MONTHS[today.month],
                "Hari ini • " + today.year + " H",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        renderHijriCalendar();
                    }
                }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addHorizontalGap(row, 10);
        Calendar now = Calendar.getInstance();
        HolidayEvent holiday = holidayFor(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        row.addView(calendarPreviewCard("Kalender Indonesia",
                new SimpleDateFormat("d MMM", indonesia).format(now.getTime()),
                holiday == null ? "Hari kerja/libur biasa" : holiday.name,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        renderIndonesiaCalendar();
                    }
                }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        page.addView(row);
    }

    private LinearLayout calendarPreviewCard(String title, String main, String subtitle, View.OnClickListener listener) {
        LinearLayout card = card(Color.WHITE);
        card.setClickable(true);
        card.setOnClickListener(listener);
        card.addView(text(title, 14, color("#172026"), Typeface.BOLD));
        card.addView(text(main, 22, color("#0F766E"), Typeface.BOLD));
        TextView sub = text(subtitle, 12, color("#4B635E"), Typeface.NORMAL);
        sub.setMaxLines(2);
        card.addView(sub);
        return card;
    }

    private void addCalendarHero(LinearLayout page, HijriDate today) {
        LinearLayout hero = gradientCard(color("#0F766E"), color("#14532D"));
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        hero.addView(text("Hari ini", 14, color("#D9F99D"), Typeface.BOLD));
        hero.addView(text(today.day + " " + HIJRI_MONTHS[today.month], 34, Color.WHITE, Typeface.BOLD));
        hero.addView(text(today.year + " Hijriah", 18, color("#FDE68A"), Typeface.BOLD));
        hero.addView(text(gregorianLabel(Calendar.getInstance()), 14, color("#DFFBF4"), Typeface.NORMAL));
        addSpacer(hero, 10);
        UpcomingEvent nextEvent = nextHijriEvent();
        if (nextEvent != null) {
            hero.addView(text(nextEvent.event.name + " - " + daysLabel(nextEvent.daysLeft), 15, Color.WHITE, Typeface.BOLD));
        }
        page.addView(hero);
    }

    private void addMonthNavigator(LinearLayout page) {
        LinearLayout nav = card(Color.WHITE);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        Button previous = compactButton("<", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveHijriMonth(-1);
            }
        });
        nav.addView(previous, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView title = text(HIJRI_MONTHS[displayedHijriMonth] + " " + displayedHijriYear + " H",
                18, color("#172026"), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        nav.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button next = compactButton(">", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveHijriMonth(1);
            }
        });
        nav.addView(next, new LinearLayout.LayoutParams(dp(44), dp(44)));
        page.addView(nav);
    }

    private void addMonthGrid(LinearLayout page, HijriDate today) {
        LinearLayout card = card(Color.WHITE);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        for (String weekday : WEEKDAYS) {
            TextView textView = text(weekday, 12, color("#4B635E"), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            header.addView(textView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        card.addView(header);
        addSpacer(card, 6);

        int firstWeekday = firstWeekdayOfHijriMonth(displayedHijriYear, displayedHijriMonth);
        int length = HijriDate.monthLength(displayedHijriYear, displayedHijriMonth);
        int day = 1;
        for (int row = 0; row < 6 && day <= length; row++) {
            LinearLayout week = new LinearLayout(this);
            week.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column < 7; column++) {
                if ((row == 0 && column < firstWeekday) || day > length) {
                    week.addView(new View(this), new LinearLayout.LayoutParams(0, dp(58), 1f));
                    continue;
                }
                week.addView(calendarDayCell(day, today), new LinearLayout.LayoutParams(0, dp(58), 1f));
                day++;
            }
            card.addView(week);
            if (day <= length) {
                addSpacer(card, 4);
            }
        }
        page.addView(card);
    }

    private View calendarDayCell(int day, HijriDate today) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(2), dp(4), dp(2), dp(4));
        boolean isToday = displayedHijriYear == today.year && displayedHijriMonth == today.month && day == today.day;
        HijriEvent event = eventFor(displayedHijriMonth, day);
        int background = isToday ? color("#0F766E") : event == null ? Color.TRANSPARENT : color("#FFF7E6");
        int stroke = isToday ? color("#0F766E") : event == null ? color("#D8E7E2") : color("#E6C773");
        cell.setBackground(rounded(background, 8, stroke));

        TextView hijri = text(String.valueOf(day), 16, isToday ? Color.WHITE : color("#172026"), Typeface.BOLD);
        hijri.setGravity(Gravity.CENTER);
        cell.addView(hijri);

        Calendar gregorian = HijriDate.toGregorian(displayedHijriYear, displayedHijriMonth, day);
        TextView miladi = text(isToday ? "Hari ini" : new SimpleDateFormat("d MMM", indonesia).format(gregorian.getTime()),
                10, isToday ? color("#DFFBF4") : color("#4B635E"), Typeface.NORMAL);
        miladi.setGravity(Gravity.CENTER);
        cell.addView(miladi);
        return cell;
    }

    private void addUpcomingEvents(LinearLayout page) {
        LinearLayout card = card(Color.WHITE);
        card.addView(text("Hari besar terdekat", 18, color("#172026"), Typeface.BOLD));
        addSpacer(card, 8);
        ArrayList<UpcomingEvent> events = upcomingHijriEvents(5);
        for (UpcomingEvent item : events) {
            card.addView(eventRow(item));
            addSpacer(card, 8);
        }
        page.addView(card);
    }

    private void addIndonesiaCalendarHero(LinearLayout page, Calendar today) {
        LinearLayout hero = gradientCard(color("#0F766E"), color("#14532D"));
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        hero.addView(text("Hari ini", 14, color("#D9F99D"), Typeface.BOLD));
        hero.addView(text(new SimpleDateFormat("dd MMMM", indonesia).format(today.getTime()),
                34, Color.WHITE, Typeface.BOLD));
        hero.addView(text(String.valueOf(today.get(Calendar.YEAR)), 18, color("#FDE68A"), Typeface.BOLD));
        HolidayEvent holiday = holidayFor(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
        hero.addView(text(holiday == null ? "Tidak ada hari libur nasional hari ini" : holiday.name,
                15, Color.WHITE, Typeface.BOLD));
        page.addView(hero);
    }

    private void addGregorianMonthNavigator(LinearLayout page) {
        LinearLayout nav = card(Color.WHITE);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        Button previous = compactButton("<", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveGregorianMonth(-1);
            }
        });
        nav.addView(previous, new LinearLayout.LayoutParams(dp(44), dp(44)));
        Calendar titleCalendar = Calendar.getInstance();
        titleCalendar.set(displayedGregorianYear, displayedGregorianMonth - 1, 1);
        TextView title = text(new SimpleDateFormat("MMMM yyyy", indonesia).format(titleCalendar.getTime()),
                18, color("#172026"), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        nav.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button next = compactButton(">", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveGregorianMonth(1);
            }
        });
        nav.addView(next, new LinearLayout.LayoutParams(dp(44), dp(44)));
        page.addView(nav);
    }

    private void addGregorianMonthGrid(LinearLayout page, Calendar today) {
        LinearLayout card = card(Color.WHITE);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        for (String weekday : WEEKDAYS) {
            TextView textView = text(weekday, 12, color("#4B635E"), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            header.addView(textView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        card.addView(header);
        addSpacer(card, 6);

        Calendar first = Calendar.getInstance();
        first.clear();
        first.set(displayedGregorianYear, displayedGregorianMonth - 1, 1);
        int firstWeekday = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int length = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = 1;
        for (int row = 0; row < 6 && day <= length; row++) {
            LinearLayout week = new LinearLayout(this);
            week.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column < 7; column++) {
                if ((row == 0 && column < firstWeekday) || day > length) {
                    week.addView(new View(this), new LinearLayout.LayoutParams(0, dp(62), 1f));
                    continue;
                }
                week.addView(gregorianDayCell(day, today), new LinearLayout.LayoutParams(0, dp(62), 1f));
                day++;
            }
            card.addView(week);
            if (day <= length) {
                addSpacer(card, 4);
            }
        }
        page.addView(card);
    }

    private View gregorianDayCell(int day, Calendar today) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(2), dp(4), dp(2), dp(4));
        boolean isToday = displayedGregorianYear == today.get(Calendar.YEAR)
                && displayedGregorianMonth == today.get(Calendar.MONTH) + 1
                && day == today.get(Calendar.DAY_OF_MONTH);
        HolidayEvent holiday = holidayFor(displayedGregorianYear, displayedGregorianMonth, day);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.clear();
        dateCalendar.set(displayedGregorianYear, displayedGregorianMonth - 1, day);
        boolean weekend = dateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || dateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        int background = isToday ? color("#0F766E") : holiday != null ? color("#FFF7E6") : weekend ? color("#E8F3EF") : Color.TRANSPARENT;
        int stroke = isToday ? color("#0F766E") : holiday != null ? color("#E6C773") : weekend ? color("#BBD8D0") : color("#D8E7E2");
        cell.setBackground(rounded(background, 8, stroke));

        TextView date = text(String.valueOf(day), 16, isToday ? Color.WHITE : color("#172026"), Typeface.BOLD);
        date.setGravity(Gravity.CENTER);
        cell.addView(date);

        String noteLabel = isToday ? "Hari ini" : holiday != null ? (holiday.jointLeave ? "Cuti" : "Libur") : weekend ? "Akhir" : "";
        int noteColor = isToday ? color("#DFFBF4") : holiday != null ? color("#8A6D1D") : color("#4B635E");
        TextView note = text(noteLabel, 10, noteColor, Typeface.BOLD);
        note.setGravity(Gravity.CENTER);
        cell.addView(note);
        return cell;
    }

    private void addHolidayList(LinearLayout page) {
        LinearLayout card = card(Color.WHITE);
        card.addView(text("Libur bulan ini", 18, color("#172026"), Typeface.BOLD));
        addSpacer(card, 8);
        boolean any = false;
        for (HolidayEvent event : ID_HOLIDAYS_2026) {
            if (event.year == displayedGregorianYear && event.month == displayedGregorianMonth) {
                any = true;
                card.addView(holidayRow(event));
                addSpacer(card, 8);
            }
        }
        if (!any) {
            card.addView(text("Tidak ada libur nasional/cuti bersama di bulan ini.", 14, color("#4B635E"), Typeface.NORMAL));
        }
        page.addView(card);
    }

    private View holidayRow(HolidayEvent event) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setBackground(rounded(event.jointLeave ? color("#E8F3EF") : color("#FFF7E6"), 8,
                event.jointLeave ? color("#BBD8D0") : color("#E6C773")));

        TextView date = text(String.valueOf(event.day), 22, event.jointLeave ? color("#0F766E") : color("#8A6D1D"), Typeface.BOLD);
        date.setGravity(Gravity.CENTER);
        row.addView(date, new LinearLayout.LayoutParams(dp(46), ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, 0, 0);
        labels.addView(text(event.name, 16, color("#172026"), Typeface.BOLD));
        labels.addView(text(event.jointLeave ? "Cuti bersama" : "Libur nasional", 13, color("#4B635E"), Typeface.NORMAL));
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private View eventRow(UpcomingEvent item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setBackground(rounded(color("#E8F3EF"), 8));

        LinearLayout dateBox = new LinearLayout(this);
        dateBox.setOrientation(LinearLayout.VERTICAL);
        dateBox.setGravity(Gravity.CENTER);
        dateBox.setBackground(rounded(color("#0F766E"), 8));
        dateBox.addView(text(String.valueOf(item.hijriDate.day), 20, Color.WHITE, Typeface.BOLD));
        dateBox.addView(text(shortMonth(item.hijriDate.month), 11, color("#D9F99D"), Typeface.BOLD));
        row.addView(dateBox, new LinearLayout.LayoutParams(dp(64), dp(58)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, 0, 0);
        labels.addView(text(item.event.name, 16, color("#172026"), Typeface.BOLD));
        labels.addView(text(daysLabel(item.daysLeft) + " - " + dateShort(item.gregorianDate),
                13, color("#4B635E"), Typeface.NORMAL));
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private void addNextPrayerCard(LinearLayout page, NextPrayerInfo next) {
        LinearLayout card = gradientCard(color("#0F766E"), color("#14532D"));
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text("Shalat berikutnya", 14, color("#D9F99D"), Typeface.BOLD));
        card.addView(text(next == null || next.prayerTime == null ? "-" : next.prayerTime.name,
                34, Color.WHITE, Typeface.BOLD));
        card.addView(text(next == null || next.prayerTime == null ? "--:--" : next.prayerTime.label() + (next.tomorrow ? " besok" : ""),
                24, color("#FDE68A"), Typeface.BOLD));
        countdownText = text("--:--:--", 18, Color.WHITE, Typeface.BOLD);
        card.addView(countdownText);
        if (next != null && next.prayerTime != null) {
            nextPrayerMillis = next.prayerTime.timeMillis;
            nextPrayerName = next.prayerTime.name;
        }
        page.addView(card);
        clockHandler.post(clockRunnable);
    }

    private View prayerRow(PrayerTime prayerTime) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(text(prayerTime.name, 16, color("#172026"), prayerTime.notify ? Typeface.BOLD : Typeface.NORMAL));
        if (prayerTime.notify) {
            labels.addView(text(reminderModeLabel(Preferences.getPrayerMode(this, prayerTime.name)),
                    12, reminderModeColor(Preferences.getPrayerMode(this, prayerTime.name)), Typeface.BOLD));
        } else {
            labels.addView(text("Info matahari", 12, color("#4B635E"), Typeface.NORMAL));
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView time = text(prayerTime.label(), 18, prayerTime.notify ? color("#0F766E") : color("#8A6D1D"), Typeface.BOLD);
        time.setGravity(Gravity.END);
        row.addView(time);
        return row;
    }

    private void addAdhanControlCard(LinearLayout page) {
        LinearLayout card = card(Color.WHITE);
        card.addView(text("Adzan & notifikasi", 18, color("#172026"), Typeface.BOLD));
        card.addView(text("Nada: " + AdhanReceiver.toneLabel(Preferences.getTone(this)), 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(card, 8);

        Switch adhanSwitch = new Switch(this);
        adhanSwitch.setText(R.string.setting_adhan_enabled);
        adhanSwitch.setTextSize(15);
        adhanSwitch.setTextColor(color("#172026"));
        adhanSwitch.setChecked(Preferences.isAdhanEnabled(this));
        adhanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setAdhanEnabled(MainActivity.this, isChecked);
                if (isChecked) {
                    ensureNotificationPermission();
                    askExactAlarmIfNeeded();
                    PrayerScheduler.scheduleNextAlarms(MainActivity.this);
                    toast("Pengingat aktif");
                } else {
                    PrayerScheduler.cancelAlarms(MainActivity.this);
                    toast("Pengingat mati");
                }
            }
        });
        card.addView(adhanSwitch);
        addSpacer(card, 8);
        card.addView(secondaryButton("Coba notifikasi", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTestPrayerReminder(false);
            }
        }));
        page.addView(card);
    }

    private void addLocationCard(LinearLayout page) {
        LinearLayout card = card(Color.WHITE);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("Lokasi", 18, color("#172026"), Typeface.BOLD),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        String source = Preferences.isManualLocation(this) ? "Manual" : Preferences.hasLocation(this) ? "GPS" : "Default";
        header.addView(text(source, 12, color("#0F766E"), Typeface.BOLD));
        card.addView(header);
        addSpacer(card, 6);

        String detail = Preferences.hasLocation(this)
                ? (Preferences.isManualLocation(this) ? "Lokasi manual: " : "") + Preferences.getLocationName(this)
                : hasLocationPermission() ? "Izin lokasi aktif. Perbarui bila perlu." : "Pakai GPS agar jadwal sesuai kota/kecamatan.";
        long time = Preferences.getLocationTime(this);
        if (Preferences.hasLocation(this) && time > 0) {
            detail += "\nUpdate " + new SimpleDateFormat("dd MMM HH:mm", indonesia).format(time);
        }
        card.addView(text(detail, 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(card, 10);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button locationButton = primaryButton(hasLocationPermission() ? "Perbarui GPS" : "Izinkan GPS", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationPermissionAndFetch();
            }
        });
        buttons.addView(locationButton, new LinearLayout.LayoutParams(0, dp(46), 1f));
        addHorizontalGap(buttons, 8);
        Button manualButton = secondaryButton("Set manual", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showManualLocationDialog();
            }
        });
        buttons.addView(manualButton, new LinearLayout.LayoutParams(0, dp(46), 1f));
        card.addView(buttons);
        page.addView(card);
    }

    private void addQiblaCard(LinearLayout page) {
        currentQiblaBearing = qiblaBearing(Preferences.getLatitude(this), Preferences.getLongitude(this));
        LinearLayout card = card(Color.WHITE);
        card.addView(text("Arah Kiblat", 18, color("#172026"), Typeface.BOLD));
        card.addView(text("Gunakan sambil posisi ponsel datar.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(card, 12);

        LinearLayout compass = new LinearLayout(this);
        compass.setOrientation(LinearLayout.HORIZONTAL);
        compass.setGravity(Gravity.CENTER_VERTICAL);

        qiblaNeedle = text("\u2191", 52, color("#0F766E"), Typeface.BOLD);
        qiblaNeedle.setGravity(Gravity.CENTER);
        qiblaNeedle.setBackground(rounded(color("#E8F3EF"), 48, color("#BBD8D0")));
        qiblaNeedle.setRotation((float) currentQiblaBearing);
        compass.addView(qiblaNeedle, new LinearLayout.LayoutParams(dp(96), dp(96)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(16), 0, 0, 0);
        labels.addView(text(Math.round(currentQiblaBearing) + "\u00B0 dari utara", 22, color("#172026"), Typeface.BOLD));
        qiblaStatus = text(compassAvailable() ? "Ikuti arah panah" : "Sensor kompas tidak tersedia",
                14, color("#4B635E"), Typeface.NORMAL);
        labels.addView(qiblaStatus);
        compass.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(compass);
        page.addView(card);
    }

    private void addQiblaHero(LinearLayout page) {
        currentQiblaBearing = qiblaBearing(Preferences.getLatitude(this), Preferences.getLongitude(this));
        LinearLayout hero = gradientCard(color("#0F766E"), color("#14532D"));
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        hero.addView(text("Arah Kiblat", 15, color("#D9F99D"), Typeface.BOLD));
        hero.addView(text(Math.round(currentQiblaBearing) + "\u00B0 dari utara", 32, Color.WHITE, Typeface.BOLD));
        hero.addView(text(Preferences.getLocationName(this), 14, color("#DFFBF4"), Typeface.NORMAL));
        addSpacer(hero, 16);

        LinearLayout compass = new LinearLayout(this);
        compass.setOrientation(LinearLayout.VERTICAL);
        compass.setGravity(Gravity.CENTER);
        compass.setPadding(dp(12), dp(12), dp(12), dp(12));
        compass.setBackground(rounded(0x22FFFFFF, 120, color("#D9F99D")));

        qiblaNeedle = text("\u2191", 88, Color.WHITE, Typeface.BOLD);
        qiblaNeedle.setGravity(Gravity.CENTER);
        qiblaNeedle.setRotation((float) currentQiblaBearing);
        compass.addView(qiblaNeedle, new LinearLayout.LayoutParams(dp(150), dp(130)));

        qiblaStatus = text(compassAvailable() ? "Putar ponsel sampai panah tepat" : "Sensor kompas tidak tersedia",
                14, color("#DFFBF4"), Typeface.BOLD);
        qiblaStatus.setGravity(Gravity.CENTER);
        compass.addView(qiblaStatus, fullWidthParams());
        hero.addView(compass, fullWidthParams());
        page.addView(hero);
    }

    private LinearLayout actionButton(String title, String subtitle, int iconRes, View.OnClickListener listener) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setBackground(rounded(Color.WHITE, 8));
        button.setClickable(true);
        button.setOnClickListener(listener);
        button.setMinimumHeight(dp(78));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color("#0F766E"));
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(rounded(color("#E8F3EF"), 22));
        button.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, 0, 0);
        labels.addView(text(title, 16, color("#172026"), Typeface.BOLD));
        labels.addView(text(subtitle, 12, color("#4B635E"), Typeface.NORMAL));
        button.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return button;
    }

    private RadioGroup toneGroup() {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        addRadio(group, "Adzan default", Preferences.TONE_ADHAN_SOFT, Preferences.getTone(this));
        addRadio(group, "Adzan Madinah", Preferences.TONE_ADHAN_CLASSIC, Preferences.getTone(this));
        addRadio(group, "Hening", Preferences.TONE_SILENT, Preferences.getTone(this));
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton checked = radioGroup.findViewById(checkedId);
                if (checked == null || checked.getTag() == null) {
                    return;
                }
                Preferences.setTone(MainActivity.this, checked.getTag().toString());
                if (Preferences.isAdhanEnabled(MainActivity.this)) {
                    PrayerScheduler.scheduleNextAlarms(MainActivity.this);
                }
                toast("Nada: " + checked.getText());
            }
        });
        return group;
    }

    private RadioGroup methodGroup() {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        addRadio(group, "Kemenag Indonesia", Preferences.METHOD_KEMENAG, Preferences.getMethod(this));
        addRadio(group, "Muslim World League", Preferences.METHOD_MWL, Preferences.getMethod(this));
        addRadio(group, "Umm Al-Qura", Preferences.METHOD_UMM_AL_QURA, Preferences.getMethod(this));
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton checked = radioGroup.findViewById(checkedId);
                if (checked == null || checked.getTag() == null) {
                    return;
                }
                Preferences.setMethod(MainActivity.this, checked.getTag().toString());
                if (Preferences.isAdhanEnabled(MainActivity.this)) {
                    PrayerScheduler.scheduleNextAlarms(MainActivity.this);
                }
                toast("Metode: " + checked.getText());
            }
        });
        return group;
    }

    private RadioGroup themeGroup() {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);
        addRadio(group, "Terang", Preferences.THEME_LIGHT, Preferences.getTheme(this));
        addRadio(group, "Gelap", Preferences.THEME_DARK, Preferences.getTheme(this));
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton checked = radioGroup.findViewById(checkedId);
                if (checked == null || checked.getTag() == null) {
                    return;
                }
                Preferences.setTheme(MainActivity.this, checked.getTag().toString());
                renderSettings();
            }
        });
        return group;
    }

    private void addPrayerModeRows(LinearLayout parent) {
        for (String prayer : PRAYERS) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));
            row.addView(text(prayer, 16, color("#172026"), Typeface.BOLD));

            RadioGroup group = new RadioGroup(this);
            group.setOrientation(RadioGroup.HORIZONTAL);
            String current = Preferences.getPrayerMode(this, prayer);
            addRadio(group, "Adzan", Preferences.PRAYER_MODE_ADHAN, current);
            addRadio(group, "Notif", Preferences.PRAYER_MODE_NOTIFY, current);
            addRadio(group, "Mati", Preferences.PRAYER_MODE_OFF, current);
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    RadioButton checked = radioGroup.findViewById(checkedId);
                    if (checked == null || checked.getTag() == null) {
                        return;
                    }
                    Preferences.setPrayerMode(MainActivity.this, prayer, checked.getTag().toString());
                    if (Preferences.isAdhanEnabled(MainActivity.this)) {
                        PrayerScheduler.scheduleNextAlarms(MainActivity.this);
                    }
                }
            });
            row.addView(group);
            parent.addView(row, fullWidthParams());
        }
    }

    private void addRadio(RadioGroup group, String label, String value, String current) {
        RadioButton radio = new RadioButton(this);
        radio.setText(label);
        radio.setTextSize(15);
        radio.setTextColor(color("#172026"));
        radio.setTag(value);
        radio.setId(View.generateViewId());
        radio.setChecked(value.equals(current));
        group.addView(radio);
    }

    private void sendTestPrayerReminder(boolean withAdhan) {
        if (!ensureNotificationPermission()) {
            return;
        }
        Intent intent = new Intent(this, com.muslimtime.app.notify.AdhanReceiver.class);
        intent.setAction(PrayerScheduler.ACTION_ADHAN);
        intent.putExtra(PrayerScheduler.EXTRA_PRAYER_NAME, "Shalat");
        intent.putExtra(PrayerScheduler.EXTRA_PRAYER_TIME, "Tes");
        intent.putExtra(PrayerScheduler.EXTRA_REMINDER_MODE,
                withAdhan ? Preferences.PRAYER_MODE_ADHAN : Preferences.PRAYER_MODE_NOTIFY);
        sendBroadcast(intent);
    }

    private void stopAdhanPlayback() {
        Intent intent = new Intent(this, AdhanPlaybackService.class);
        intent.setAction(AdhanPlaybackService.ACTION_STOP);
        try {
            startService(intent);
        } catch (Exception ignored) {
        }
    }

    private void requestInitialLocationIfNeeded() {
        if (Preferences.hasLocation(this)) {
            registerSignificantLocationMonitor();
            return;
        }
        if (hasLocationPermission()) {
            fetchLocation();
            return;
        }
        if (!Preferences.wasLocationPermissionAsked(this)) {
            requestLocationPermissionAndFetch();
        }
    }

    private void requestLocationPermissionAndFetch() {
        if (!hasLocationPermission()) {
            Preferences.setLocationPermissionAsked(this);
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQ_LOCATION);
            return;
        }
        fetchLocation();
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            toast("GPS tidak tersedia");
            return;
        }

        Location fresh = bestFreshLocation(manager);
        if (fresh != null) {
            saveGpsLocation(fresh);
            return;
        }

        String provider = preferredSingleProvider(manager);
        if (provider == null) {
            toast("Aktifkan lokasi dulu");
            return;
        }

        toast("Mencari lokasi...");
        manager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                saveGpsLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        }, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    private Location bestFreshLocation(LocationManager manager) {
        Location best = null;
        long now = System.currentTimeMillis();
        for (String provider : manager.getProviders(true)) {
            Location candidate = manager.getLastKnownLocation(provider);
            if (candidate == null || candidate.getTime() <= 0 || now - candidate.getTime() > FRESH_LOCATION_MS) {
                continue;
            }
            if (best == null || candidate.getAccuracy() < best.getAccuracy()) {
                best = candidate;
            }
        }
        return best;
    }

    private String preferredSingleProvider(LocationManager manager) {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        if (manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return LocationManager.PASSIVE_PROVIDER;
        }
        return null;
    }

    private void saveGpsLocation(Location location) {
        LocationHelper.LocationName name = LocationHelper.resolve(this, location.getLatitude(), location.getLongitude());
        Preferences.saveLocation(this, location.getLatitude(), location.getLongitude(), name.label, name.city, false);
        registerSignificantLocationMonitor();
        if (Preferences.isAdhanEnabled(this)) {
            PrayerScheduler.scheduleNextAlarms(this);
        }
        toast("Lokasi diperbarui");
        refreshAfterLocationChange();
    }

    private void registerSignificantLocationMonitor() {
        if (!Preferences.isManualLocation(this)) {
            SignificantLocationMonitor.register(this);
        }
    }

    private void showManualLocationDialog() {
        final AlertDialog[] dialogRef = new AlertDialog[1];
        LinearLayout body = card(Color.WHITE);
        body.setPadding(dp(18), dp(18), dp(18), dp(16));
        body.addView(text("Set lokasi manual", 20, color("#172026"), Typeface.BOLD));
        body.addView(text("Ketik kota atau kecamatan.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(body, 12);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Contoh: Bandung");
        input.setTextColor(color("#172026"));
        input.setHintTextColor(color("#7B8F89"));
        input.setBackground(rounded(color("#E8F3EF"), 8, color("#BBD8D0")));
        input.setPadding(dp(12), 0, dp(12), 0);
        body.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        addSpacer(body, 10);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button cancel = secondaryButton("Batal", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogRef[0] != null) {
                    dialogRef[0].dismiss();
                }
            }
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(46), 1f));
        addHorizontalGap(actions, 8);
        Button save = primaryButton("Simpan", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resolveManualLocation(input.getText().toString(), dialogRef[0]);
            }
        });
        actions.addView(save, new LinearLayout.LayoutParams(0, dp(46), 1f));
        body.addView(actions);
        addSpacer(body, 10);

        LinearLayout presets = new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        presets.addView(presetLocationButton("Jakarta", -6.2088, 106.8456, dialogRef),
                new LinearLayout.LayoutParams(0, dp(42), 1f));
        addHorizontalGap(presets, 6);
        presets.addView(presetLocationButton("Bandung", -6.9175, 107.6191, dialogRef),
                new LinearLayout.LayoutParams(0, dp(42), 1f));
        addHorizontalGap(presets, 6);
        presets.addView(presetLocationButton("Surabaya", -7.2575, 112.7521, dialogRef),
                new LinearLayout.LayoutParams(0, dp(42), 1f));
        body.addView(presets);

        dialogRef[0] = new AlertDialog.Builder(this).create();
        dialogRef[0].setView(body);
        dialogRef[0].show();
        if (dialogRef[0].getWindow() != null) {
            dialogRef[0].getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private Button presetLocationButton(String city, double lat, double lon, AlertDialog[] dialogRef) {
        return secondaryButton(city, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveManualLocation(city, city, lat, lon, dialogRef[0]);
            }
        });
    }

    private void resolveManualLocation(String query, AlertDialog dialog) {
        String cleanQuery = query == null ? "" : query.trim();
        if (cleanQuery.isEmpty()) {
            toast("Isi nama lokasi");
            return;
        }
        toast("Mencari lokasi...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, indonesia);
                    List<android.location.Address> addresses = geocoder.getFromLocationName(cleanQuery + ", Indonesia", 1);
                    if (addresses == null || addresses.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toast("Lokasi tidak ditemukan");
                            }
                        });
                        return;
                    }
                    android.location.Address address = addresses.get(0);
                    LocationHelper.LocationName name = LocationHelper.fromAddress(
                            address,
                            address.getLatitude(),
                            address.getLongitude()
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            saveManualLocation(name.label, name.city, address.getLatitude(), address.getLongitude(), dialog);
                        }
                    });
                } catch (Exception exception) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast("Gagal mencari lokasi");
                        }
                    });
                }
            }
        }).start();
    }

    private void saveManualLocation(String label, String city, double lat, double lon, AlertDialog dialog) {
        Preferences.saveLocation(this, lat, lon, label, city, true);
        SignificantLocationMonitor.unregister(this);
        if (Preferences.isAdhanEnabled(this)) {
            PrayerScheduler.scheduleNextAlarms(this);
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        toast("Lokasi manual disimpan");
        refreshAfterLocationChange();
    }

    private void refreshAfterLocationChange() {
        if (currentScreen == SCREEN_HOME) {
            renderHome();
        } else if (currentScreen == SCREEN_SCHEDULE) {
            renderSchedule();
        } else if (currentScreen == SCREEN_QIBLA) {
            renderQibla();
        } else if (currentScreen == SCREEN_SETTINGS) {
            renderSettings();
        }
    }

    private boolean ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            return true;
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATION);
        return false;
    }

    private void askExactAlarmIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null || alarmManager.canScheduleExactAlarms()) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && hasLocationPermission()) {
            fetchLocation();
        }
        if (requestCode == REQ_NOTIFICATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            PrayerScheduler.scheduleNextAlarms(this);
        }
    }

    private List<PrayerTime> todayPrayerTimes() {
        return prayerTimesFor(Calendar.getInstance());
    }

    private List<PrayerTime> prayerTimesFor(Calendar date) {
        return PrayerTimesCalculator.calculate(
                date,
                Preferences.getLatitude(this),
                Preferences.getLongitude(this),
                Preferences.getMethod(this)
        );
    }

    private RamadanInfo ramadanInfo(Calendar date) {
        Calendar day = (Calendar) date.clone();
        HijriDate hijri = HijriDate.fromGregorian(day);
        List<PrayerTime> times = prayerTimesFor(day);
        PrayerTime subuhPrayer = prayerByName(times, "Subuh");
        PrayerTime maghribPrayer = prayerByName(times, "Maghrib");
        PrayerTime isyaPrayer = prayerByName(times, "Isya");

        Calendar subuh = calendarFromMillis(subuhPrayer == null ? System.currentTimeMillis() : subuhPrayer.timeMillis);
        Calendar imsak = shiftedCalendar(subuh, -IMSAK_OFFSET_MINUTES);
        Calendar sahurStart = shiftedCalendar(imsak, -SAHUR_WINDOW_MINUTES);
        Calendar buka = calendarFromMillis(maghribPrayer == null ? System.currentTimeMillis() : maghribPrayer.timeMillis);
        Calendar isya = calendarFromMillis(isyaPrayer == null ? System.currentTimeMillis() : isyaPrayer.timeMillis);
        boolean inRamadan = hijri.month == 9;

        Calendar target = null;
        String targetName = null;
        if (inRamadan) {
            long now = System.currentTimeMillis();
            if (now < sahurStart.getTimeInMillis()) {
                target = sahurStart;
                targetName = "Sahur";
            } else if (now < imsak.getTimeInMillis()) {
                target = imsak;
                targetName = "Imsak";
            } else if (now < buka.getTimeInMillis()) {
                target = buka;
                targetName = "Buka puasa";
            } else {
                Calendar tomorrow = (Calendar) day.clone();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                RamadanInfo tomorrowInfo = ramadanInfoForTarget(tomorrow);
                target = tomorrowInfo.sahurStart;
                targetName = "Sahur besok";
            }
        }

        return new RamadanInfo(hijri, inRamadan, daysUntilHijriDate(9, 1),
                sahurStart, imsak, subuh, buka, isya, target, targetName);
    }

    private RamadanInfo ramadanInfoForTarget(Calendar date) {
        HijriDate hijri = HijriDate.fromGregorian(date);
        List<PrayerTime> times = prayerTimesFor(date);
        PrayerTime subuhPrayer = prayerByName(times, "Subuh");
        PrayerTime maghribPrayer = prayerByName(times, "Maghrib");
        PrayerTime isyaPrayer = prayerByName(times, "Isya");
        Calendar subuh = calendarFromMillis(subuhPrayer == null ? System.currentTimeMillis() : subuhPrayer.timeMillis);
        Calendar imsak = shiftedCalendar(subuh, -IMSAK_OFFSET_MINUTES);
        Calendar sahurStart = shiftedCalendar(imsak, -SAHUR_WINDOW_MINUTES);
        Calendar buka = calendarFromMillis(maghribPrayer == null ? System.currentTimeMillis() : maghribPrayer.timeMillis);
        Calendar isya = calendarFromMillis(isyaPrayer == null ? System.currentTimeMillis() : isyaPrayer.timeMillis);
        return new RamadanInfo(hijri, hijri.month == 9, daysUntilHijriDate(9, 1),
                sahurStart, imsak, subuh, buka, isya, sahurStart, "Sahur");
    }

    private PrayerTime prayerByName(List<PrayerTime> times, String name) {
        for (PrayerTime time : times) {
            if (name.equals(time.name)) {
                return time;
            }
        }
        return null;
    }

    private Calendar calendarFromMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    private Calendar shiftedCalendar(Calendar calendar, int minutes) {
        Calendar shifted = (Calendar) calendar.clone();
        shifted.add(Calendar.MINUTE, minutes);
        return shifted;
    }

    private void setRamadanTarget(RamadanInfo info) {
        if (info.target == null || info.targetName == null) {
            return;
        }
        ramadanTargetMillis = info.target.getTimeInMillis();
        ramadanTargetName = info.targetName;
    }

    private int daysUntilHijriDate(int month, int day) {
        Calendar cursor = startOfDay(Calendar.getInstance());
        for (int i = 0; i <= 390; i++) {
            HijriDate hijri = HijriDate.fromGregorian(cursor);
            if (hijri.month == month && hijri.day == day) {
                return i;
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return -1;
    }

    private NextPrayerInfo nextPrayer(List<PrayerTime> todayTimes) {
        long now = System.currentTimeMillis();
        for (PrayerTime prayerTime : todayTimes) {
            if (prayerTime.notify && prayerTime.timeMillis > now) {
                return new NextPrayerInfo(prayerTime, false);
            }
        }
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        List<PrayerTime> tomorrowTimes = PrayerTimesCalculator.calculate(
                tomorrow,
                Preferences.getLatitude(this),
                Preferences.getLongitude(this),
                Preferences.getMethod(this)
        );
        for (PrayerTime prayerTime : tomorrowTimes) {
            if (prayerTime.notify) {
                return new NextPrayerInfo(prayerTime, true);
            }
        }
        return null;
    }

    private void updateClockText() {
        if (clockText != null) {
            clockText.setText(new SimpleDateFormat("HH:mm:ss", indonesia).format(Calendar.getInstance().getTime()));
        }
        if (countdownText != null && nextPrayerMillis > 0) {
            countdownText.setText(formatRemaining(nextPrayerMillis));
        }
        if (countdownLabelText != null && nextPrayerName != null) {
            countdownLabelText.setText("Menuju " + nextPrayerName);
        }
        if (ramadanCountdownText != null && ramadanTargetMillis > 0) {
            ramadanCountdownText.setText(formatRemaining(ramadanTargetMillis));
        }
        if (ramadanCountdownLabelText != null && ramadanTargetName != null) {
            ramadanCountdownLabelText.setText("Menuju " + ramadanTargetName);
        }
    }

    private int firstWeekdayOfHijriMonth(int year, int month) {
        Calendar first = HijriDate.toGregorian(year, month, 1);
        return (first.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

    private void moveHijriMonth(int offset) {
        displayedHijriMonth += offset;
        while (displayedHijriMonth < 1) {
            displayedHijriMonth += 12;
            displayedHijriYear--;
        }
        while (displayedHijriMonth > 12) {
            displayedHijriMonth -= 12;
            displayedHijriYear++;
        }
        renderHijriCalendar();
    }

    private void moveGregorianMonth(int offset) {
        displayedGregorianMonth += offset;
        while (displayedGregorianMonth < 1) {
            displayedGregorianMonth += 12;
            displayedGregorianYear--;
        }
        while (displayedGregorianMonth > 12) {
            displayedGregorianMonth -= 12;
            displayedGregorianYear++;
        }
        renderIndonesiaCalendar();
    }

    private HolidayEvent holidayFor(int year, int month, int day) {
        for (HolidayEvent event : ID_HOLIDAYS_2026) {
            if (event.year == year && event.month == month && event.day == day) {
                return event;
            }
        }
        return null;
    }

    private HijriEvent eventFor(int month, int day) {
        for (HijriEvent event : HIJRI_EVENTS) {
            if (event.month == month && event.day == day) {
                return event;
            }
        }
        return null;
    }

    private UpcomingEvent nextHijriEvent() {
        ArrayList<UpcomingEvent> events = upcomingHijriEvents(1);
        return events.isEmpty() ? null : events.get(0);
    }

    private ArrayList<UpcomingEvent> upcomingHijriEvents(int limit) {
        ArrayList<UpcomingEvent> result = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        Calendar cursor = startOfDay(Calendar.getInstance());
        Calendar today = startOfDay(Calendar.getInstance());
        for (int i = 0; i <= 390 && result.size() < limit; i++) {
            HijriDate hijri = HijriDate.fromGregorian(cursor);
            HijriEvent event = eventFor(hijri.month, hijri.day);
            if (event != null) {
                String key = hijri.year + ":" + event.month + ":" + event.day;
                if (!seen.contains(key)) {
                    seen.add(key);
                    result.add(new UpcomingEvent(event, hijri, (Calendar) cursor.clone(), daysBetween(today, cursor)));
                }
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    private static final class UpcomingEvent {
        final HijriEvent event;
        final HijriDate hijriDate;
        final Calendar gregorianDate;
        final long daysLeft;

        UpcomingEvent(HijriEvent event, HijriDate hijriDate, Calendar gregorianDate, long daysLeft) {
            this.event = event;
            this.hijriDate = hijriDate;
            this.gregorianDate = gregorianDate;
            this.daysLeft = daysLeft;
        }
    }

    private Calendar startOfDay(Calendar calendar) {
        Calendar copy = (Calendar) calendar.clone();
        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MILLISECOND, 0);
        return copy;
    }

    private long daysBetween(Calendar start, Calendar end) {
        return (startOfDay(end).getTimeInMillis() - startOfDay(start).getTimeInMillis()) / 86_400_000L;
    }

    private String daysLabel(long days) {
        if (days <= 0) {
            return "Hari ini";
        }
        if (days == 1) {
            return "Besok";
        }
        return days + " hari lagi";
    }

    private String shortMonth(int month) {
        String value = HIJRI_MONTHS[month];
        return value.length() <= 5 ? value : value.substring(0, 5);
    }

    private String shortPrayerName(String name) {
        if ("Dzuhur".equals(name)) {
            return "Dzhr";
        }
        if ("Maghrib".equals(name)) {
            return "Mgh";
        }
        return name.length() <= 5 ? name : name.substring(0, 5);
    }

    private String gregorianLabel(Calendar calendar) {
        return new SimpleDateFormat("EEEE, dd MMMM yyyy", indonesia).format(calendar.getTime());
    }

    private String dateShort(Calendar calendar) {
        return new SimpleDateFormat("dd MMM yyyy", indonesia).format(calendar.getTime());
    }

    private String timeLabel(Calendar calendar) {
        return new SimpleDateFormat("HH:mm", indonesia).format(calendar.getTime());
    }

    private String nextRamadanText(RamadanInfo info) {
        if (info.daysToRamadan < 0) {
            return "Ramadhan";
        }
        if (info.daysToRamadan == 0) {
            return "Ramadhan hari ini";
        }
        if (info.daysToRamadan == 1) {
            return "Ramadhan besok";
        }
        return info.daysToRamadan + " hari lagi";
    }

    private String formatRemaining(long targetMillis) {
        long remaining = Math.max(0L, targetMillis - System.currentTimeMillis());
        long seconds = remaining / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
    }

    private void registerCompass() {
        if (sensorManager == null || qiblaNeedle == null) {
            return;
        }
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private boolean compassAvailable() {
        return accelerometer != null && magnetometer != null;
    }

    private void updateQiblaNeedle(float azimuth) {
        if (qiblaNeedle == null) {
            return;
        }
        qiblaNeedle.setRotation((float) currentQiblaBearing - azimuth);
        if (qiblaStatus != null) {
            qiblaStatus.setText(R.string.qibla_follow_arrow);
        }
    }

    private double qiblaBearing(double lat, double lon) {
        double phiK = Math.toRadians(KAABA_LAT);
        double lambdaK = Math.toRadians(KAABA_LON);
        double phi = Math.toRadians(lat);
        double lambda = Math.toRadians(lon);
        double y = Math.sin(lambdaK - lambda);
        double x = Math.cos(phi) * Math.tan(phiK) - Math.sin(phi) * Math.cos(lambdaK - lambda);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (!handleBackNavigation()) {
            confirmExitApp();
        }
    }

    private boolean handleBackNavigation() {
        if (currentScreen != SCREEN_HOME) {
            stopAdhanPlayback();
            renderHome();
            return true;
        }
        return false;
    }

    private void confirmExitApp() {
        if (isFinishing()) {
            return;
        }
        if (exitDialog != null && exitDialog.isShowing()) {
            return;
        }

        LinearLayout body = card(Color.WHITE);
        body.setPadding(dp(18), dp(18), dp(18), dp(16));
        body.addView(text("Keluar aplikasi?", 20, color("#172026"), Typeface.BOLD));
        body.addView(text("Tutup Muslim Time sekarang.", 14, color("#4B635E"), Typeface.NORMAL));
        addSpacer(body, 14);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button cancel = secondaryButton("Batal", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exitDialog != null) {
                    exitDialog.dismiss();
                }
            }
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(46), 1f));

        Button exit = primaryButton("Keluar", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exitDialog != null) {
                    exitDialog.dismiss();
                }
                finish();
            }
        });
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        exitParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(exit, exitParams);
        body.addView(actions, fullWidthParams());

        exitDialog = new AlertDialog.Builder(this).create();
        exitDialog.setView(body);
        exitDialog.setOnDismissListener(dialogInterface -> exitDialog = null);
        exitDialog.show();
        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void addTopBar(LinearLayout page, String title) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        Button back = secondaryButton("Kembali", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAdhanPlayback();
                renderHome();
            }
        });
        bar.addView(back, new LinearLayout.LayoutParams(dp(104), dp(44)));

        TextView titleView = text(title, 24, color("#172026"), Typeface.BOLD);
        titleView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        bar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        page.addView(bar);
        addSpacer(page, 14);
    }

    private String methodLabel(String method) {
        if (Preferences.METHOD_MWL.equals(method)) {
            return "MWL";
        }
        if (Preferences.METHOD_UMM_AL_QURA.equals(method)) {
            return "Umm Al-Qura";
        }
        return "Kemenag";
    }

    private String reminderModeLabel(String mode) {
        if (Preferences.PRAYER_MODE_NOTIFY.equals(mode)) {
            return "Notif";
        }
        if (Preferences.PRAYER_MODE_OFF.equals(mode)) {
            return "Mati";
        }
        return "Adzan";
    }

    private int reminderModeColor(String mode) {
        if (Preferences.PRAYER_MODE_NOTIFY.equals(mode)) {
            return color("#8A6D1D");
        }
        if (Preferences.PRAYER_MODE_OFF.equals(mode)) {
            return color("#4B635E");
        }
        return color("#0F766E");
    }

    private void applySystemBars() {
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setStatusBarColor(isDarkTheme() ? color("#071C19") : color("#115E59"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void applyShellTheme() {
        if (rootFrame == null || shellWash == null || shellBackground == null) {
            return;
        }
        if (isDarkTheme()) {
            rootFrame.setBackgroundColor(color("#0B1D1A"));
            shellWash.setBackgroundColor(0xE60B1D1A);
            shellBackground.setAlpha(0.16f);
        } else {
            rootFrame.setBackgroundColor(color("#F7FAF8"));
            shellWash.setBackgroundColor(0xCCF7FAF8);
            shellBackground.setAlpha(0.38f);
        }
    }

    private boolean isDarkTheme() {
        return Preferences.isDarkTheme(this);
    }

    private LinearLayout card(int background) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(rounded(resolveBackgroundColor(background), 8));
        card.setElevation(dp(1));
        card.setLayoutParams(fullWidthParams());
        return card;
    }

    private LinearLayout gradientCard(int startColor, int endColor) {
        LinearLayout card = card(Color.WHITE);
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{startColor, endColor}
        );
        background.setCornerRadius(dp(8));
        card.setBackground(background);
        return card;
    }

    private TextView text(String value, int sp, int color, int typeface) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(color);
        textView.setTypeface(Typeface.DEFAULT, typeface);
        textView.setLineSpacing(dp(2), 1.0f);
        textView.setIncludeFontPadding(true);
        return textView;
    }

    private Button primaryButton(String label, View.OnClickListener listener) {
        Button button = baseButton(label, listener);
        button.setTextColor(isDarkTheme() ? color("#07211E") : Color.WHITE);
        button.setBackground(rounded(isDarkTheme() ? color("#35D0B6") : color("#0F766E"), 8));
        return button;
    }

    private Button secondaryButton(String label, View.OnClickListener listener) {
        Button button = baseButton(label, listener);
        button.setTextColor(color("#0F766E"));
        button.setBackground(rounded(color("#E8F3EF"), 8, color("#BBD8D0")));
        return button;
    }

    private Button compactButton(String label, View.OnClickListener listener) {
        Button button = baseButton(label, listener);
        button.setTextSize(18);
        button.setTextColor(color("#0F766E"));
        button.setBackground(rounded(color("#E8F3EF"), 8, color("#BBD8D0")));
        return button;
    }

    private Button baseButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setMinHeight(dp(44));
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setOnClickListener(listener);
        return button;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(resolveBackgroundColor(color));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = rounded(color, radiusDp);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams fullWidthParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void addSpacer(LinearLayout parent, int dpHeight) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(dpHeight)
        ));
    }

    private void addHorizontalGap(LinearLayout parent, int dpWidth) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(dp(dpWidth), 1));
    }

    private int color(String hex) {
        if (isDarkTheme()) {
            String normalized = hex.toUpperCase(Locale.US);
            if ("#F7FAF8".equals(normalized) || "#FFFEFA".equals(normalized)) {
                return Color.parseColor("#0B1D1A");
            }
            if ("#172026".equals(normalized)) {
                return Color.parseColor("#F8F3E8");
            }
            if ("#4B635E".equals(normalized) || "#405650".equals(normalized)) {
                return Color.parseColor("#C4D3CD");
            }
            if ("#0F766E".equals(normalized) || "#115E59".equals(normalized)) {
                return Color.parseColor("#35D0B6");
            }
            if ("#E8F3EF".equals(normalized)) {
                return Color.parseColor("#183A34");
            }
            if ("#BBD8D0".equals(normalized) || "#D8E7E2".equals(normalized) || "#D8EFE8".equals(normalized)) {
                return Color.parseColor("#34524C");
            }
            if ("#FFF7E6".equals(normalized)) {
                return Color.parseColor("#2A2417");
            }
            if ("#8A5A00".equals(normalized) || "#8A6D1D".equals(normalized)) {
                return Color.parseColor("#F3D27A");
            }
            if ("#E6C773".equals(normalized)) {
                return Color.parseColor("#6D5A2A");
            }
        }
        return Color.parseColor(hex);
    }

    private int resolveBackgroundColor(int background) {
        if (isDarkTheme() && background == Color.WHITE) {
            return Color.parseColor("#142A26");
        }
        return background;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

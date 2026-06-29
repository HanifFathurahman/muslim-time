package com.muslimtime.app.prayer;

import com.muslimtime.app.Preferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public final class PrayerTimesCalculator {
    private PrayerTimesCalculator() {
    }

    public static List<PrayerTime> calculate(Calendar date, double latitude, double longitude, String method) {
        MethodConfig config = MethodConfig.forMethod(method);
        Calendar base = (Calendar) date.clone();
        base.set(Calendar.HOUR_OF_DAY, 0);
        base.set(Calendar.MINUTE, 0);
        base.set(Calendar.SECOND, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar noonCalendar = (Calendar) base.clone();
        noonCalendar.set(Calendar.HOUR_OF_DAY, 12);
        double timezone = timezoneHours(noonCalendar);
        Solar solar = solarForDate(base);
        double dhuhr = fixHour(12 + timezone - (longitude / 15.0) - solar.equationOfTime);

        double sunriseAngle = hourAngleForZenith(latitude, solar.declination, 90.833);
        double sunrise = dhuhr - (sunriseAngle / 15.0);
        double sunset = dhuhr + (sunriseAngle / 15.0);
        double fajr = dhuhr - (hourAngleForZenith(latitude, solar.declination, 90.0 + config.fajrAngle) / 15.0);
        double asr = dhuhr + (hourAngleForAsr(latitude, solar.declination, config.asrFactor) / 15.0);
        double isha = config.ishaIntervalMinutes > 0
                ? sunset + (config.ishaIntervalMinutes / 60.0)
                : dhuhr + (hourAngleForZenith(latitude, solar.declination, 90.0 + config.ishaAngle) / 15.0);

        ArrayList<PrayerTime> result = new ArrayList<>();
        result.add(toPrayerTime(base, "Subuh", fajr, config.minuteOffset, true));
        result.add(toPrayerTime(base, "Terbit", sunrise, 0, false));
        result.add(toPrayerTime(base, "Dzuhur", dhuhr, config.minuteOffset, true));
        result.add(toPrayerTime(base, "Ashar", asr, config.minuteOffset, true));
        result.add(toPrayerTime(base, "Maghrib", sunset, config.minuteOffset, true));
        result.add(toPrayerTime(base, "Isya", isha, config.minuteOffset, true));
        return result;
    }

    private static PrayerTime toPrayerTime(Calendar base, String name, double decimalHour, int minuteOffset, boolean notify) {
        double fixed = fixHour(decimalHour + (minuteOffset / 60.0));
        int hour = (int) Math.floor(fixed);
        int minute = (int) Math.round((fixed - hour) * 60.0);

        if (minute >= 60) {
            minute -= 60;
            hour += 1;
        }
        if (hour >= 24) {
            hour -= 24;
        }

        Calendar time = (Calendar) base.clone();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return new PrayerTime(name, hour, minute, time.getTimeInMillis(), notify);
    }

    private static double timezoneHours(Calendar calendar) {
        TimeZone zone = calendar.getTimeZone();
        return zone.getOffset(calendar.getTimeInMillis()) / 3600000.0;
    }

    private static Solar solarForDate(Calendar calendar) {
        double julianDate = julianDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        double days = julianDate - 2451545.0;
        double meanLongitude = fixAngle(280.459 + 0.98564736 * days);
        double meanAnomaly = fixAngle(357.529 + 0.98560028 * days);
        double eclipticLongitude = fixAngle(
                meanLongitude
                        + 1.915 * sinDeg(meanAnomaly)
                        + 0.020 * sinDeg(2 * meanAnomaly)
        );
        double obliquity = 23.439 - (0.00000036 * days);
        double rightAscension = Math.toDegrees(Math.atan2(
                cosDeg(obliquity) * sinDeg(eclipticLongitude),
                cosDeg(eclipticLongitude)
        )) / 15.0;
        rightAscension = fixHour(rightAscension);

        double declination = Math.toDegrees(Math.asin(sinDeg(obliquity) * sinDeg(eclipticLongitude)));
        double equationOfTime = fixHour((meanLongitude / 15.0) - rightAscension + 12.0) - 12.0;
        return new Solar(declination, equationOfTime);
    }

    private static double julianDate(int year, int month, int day) {
        int y = year;
        int m = month;
        if (m <= 2) {
            y -= 1;
            m += 12;
        }
        int a = y / 100;
        int b = 2 - a + (a / 4);
        return Math.floor(365.25 * (y + 4716))
                + Math.floor(30.6001 * (m + 1))
                + day + b - 1524.5;
    }

    private static double hourAngleForZenith(double latitude, double declination, double zenith) {
        double value = (cosDeg(zenith) - sinDeg(latitude) * sinDeg(declination))
                / (cosDeg(latitude) * cosDeg(declination));
        return Math.toDegrees(Math.acos(clamp(value, -1.0, 1.0)));
    }

    private static double hourAngleForAsr(double latitude, double declination, int factor) {
        double diff = Math.abs(latitude - declination);
        double altitude = Math.toDegrees(Math.atan(1.0 / (factor + Math.tan(Math.toRadians(diff)))));
        double value = (sinDeg(altitude) - sinDeg(latitude) * sinDeg(declination))
                / (cosDeg(latitude) * cosDeg(declination));
        return Math.toDegrees(Math.acos(clamp(value, -1.0, 1.0)));
    }

    private static double sinDeg(double degree) {
        return Math.sin(Math.toRadians(degree));
    }

    private static double cosDeg(double degree) {
        return Math.cos(Math.toRadians(degree));
    }

    private static double fixAngle(double angle) {
        double result = angle % 360.0;
        return result < 0 ? result + 360.0 : result;
    }

    private static double fixHour(double hour) {
        double result = hour % 24.0;
        return result < 0 ? result + 24.0 : result;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Solar {
        final double declination;
        final double equationOfTime;

        Solar(double declination, double equationOfTime) {
            this.declination = declination;
            this.equationOfTime = equationOfTime;
        }
    }

    private static final class MethodConfig {
        final double fajrAngle;
        final double ishaAngle;
        final int ishaIntervalMinutes;
        final int asrFactor;
        final int minuteOffset;

        MethodConfig(double fajrAngle, double ishaAngle, int ishaIntervalMinutes, int asrFactor, int minuteOffset) {
            this.fajrAngle = fajrAngle;
            this.ishaAngle = ishaAngle;
            this.ishaIntervalMinutes = ishaIntervalMinutes;
            this.asrFactor = asrFactor;
            this.minuteOffset = minuteOffset;
        }

        static MethodConfig forMethod(String method) {
            if (Preferences.METHOD_MWL.equals(method)) {
                return new MethodConfig(18.0, 17.0, 0, 1, 1);
            }
            if (Preferences.METHOD_UMM_AL_QURA.equals(method)) {
                return new MethodConfig(18.5, 0.0, 90, 1, 1);
            }
            return new MethodConfig(20.0, 18.0, 0, 1, 2);
        }
    }
}

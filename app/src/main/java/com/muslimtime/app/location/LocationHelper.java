package com.muslimtime.app.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public final class LocationHelper {
    private LocationHelper() {
    }

    public static final class LocationName {
        public final String label;
        public final String city;

        LocationName(String label, String city) {
            this.label = label;
            this.city = city;
        }
    }

    public static LocationName resolve(Context context, double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(context, new Locale("id", "ID"));
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return fromAddress(addresses.get(0), lat, lon);
            }
        } catch (Exception ignored) {
        }
        String coordinate = String.format(Locale.US, "%.4f, %.4f", lat, lon);
        return new LocationName(coordinate, coordinate);
    }

    public static LocationName fromAddress(Address address, double lat, double lon) {
        String district = firstNonEmpty(address.getSubLocality(), address.getLocality(), address.getSubAdminArea());
        String city = firstNonEmpty(address.getLocality(), address.getSubAdminArea(), address.getAdminArea());
        if (district != null && city != null && !district.equals(city)) {
            return new LocationName(district + ", " + city, city);
        }
        if (city != null) {
            return new LocationName(city, city);
        }
        String coordinate = String.format(Locale.US, "%.4f, %.4f", lat, lon);
        return new LocationName(coordinate, coordinate);
    }

    public static float distanceMeters(double startLat, double startLon, double endLat, double endLon) {
        float[] result = new float[1];
        Location.distanceBetween(startLat, startLon, endLat, endLon, result);
        return result[0];
    }

    public static boolean sameCity(String first, String second) {
        return normalize(first).equals(normalize(second));
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US)
                .replace("kota administrasi", "")
                .replace("kota", "")
                .replace("kabupaten", "")
                .replace("kab.", "")
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }
}

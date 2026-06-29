# Muslim Time

Muslim Time adalah aplikasi Android sederhana untuk jadwal shalat, adzan/notifikasi, arah kiblat, kalender Islam, kalender Indonesia, dan Mode Ramadhan.

## Fitur

- Jadwal shalat berdasarkan lokasi.
- Notifikasi/adzan per waktu shalat.
- Arah kiblat.
- Kalender Islam.
- Kalender Indonesia dan hari libur nasional.
- Mode Ramadhan: sahur, imsak, buka puasa, dan countdown.
- Tema terang dan gelap.
- Lokasi hemat baterai: ambil GPS sekali, lalu update pasif saat lokasi berubah signifikan.

## Build

Pastikan Android SDK dan JDK 17 tersedia.

```bash
./gradlew assembleDebug
```

Untuk Windows:

```powershell
.\gradlew.bat assembleDebug
```

APK debug ada di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Release

Build release:

```powershell
.\gradlew.bat assembleRelease
```

Catatan: konfigurasi release saat ini masih memakai debug signing untuk testing lokal. Untuk publish resmi, gunakan keystore produksi dan jangan commit file keystore ke GitHub.

## Jangan Commit

File/folder berikut sengaja diabaikan:

- `.gradle/`
- `build/`
- `app/build/`
- `local.properties`
- APK/AAB hasil build
- file keystore/signing

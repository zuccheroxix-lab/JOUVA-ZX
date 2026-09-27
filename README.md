# ZX Gaming Companion 🎮

ZX Gaming Companion adalah aplikasi pendamping gaming profesional untuk Android yang dibangun menggunakan modern Kotlin, Jetpack Compose, dan Room Database. Aplikasi ini menyediakan overlay metrik performa real-time (FPS, CPU, RAM, Suhu), kustomisasi sensitivitas layar, crosshair kustom, dan perekam layar.

---

## 📦 Lokasi File APK (Build Artifacts)

File APK hasil build yang valid dan siap diunduh/diinstal tersimpan di folder `releases/`:

| Tipe APK | Lokasi File | Ukuran | Deskripsi & Status Signing |
| :--- | :--- | :--- | :--- |
| **Debug APK** | [`releases/app-debug.apk`](releases/app-debug.apk) | ~23 MB | Ditandatangani dengan keystore debug (`androiddebugkey`), cocok untuk pengujian dan development langsung. |
| **Release APK** | [`releases/app-release.apk`](releases/app-release.apk) | ~16 MB | Dioptimasi penuh (*resource shrinking & code optimization*). Ditandatangani untuk deployment aplikasi release. |

---

## 📋 Informasi Versi & Build

- **Versi Aplikasi:** `1.0` (`versionCode: 1`)
- **Package Name / Application ID:** `com.aistudio.zxgaming.kxmpzq`
- **Target SDK:** Android 36 (Android 15+)
- **Minimum SDK:** Android 24 (Android 7.0 Nougat+)
- **Build System:** Gradle (Kotlin DSL - Gradle 9.3.1 / AGP 9.1.1)
- **Kompatibilitas Runtime:** Arsitektur ARM64 / x86_64

---

## 🚀 Fitur Utama Aplikasi

1. **Dashboard Performa & Telemetri:**
   - Pemantauan FPS real-time dengan Choreographer Frame Monitor.
   - Deteksi penggunaan CPU, RAM, status memori sistem, dan suhu perangkat.
2. **Floating Overlay (Head-Up Display):**
   - Mini overlay dan full telemetry overlay di atas game dengan kontrol transparansi dan posisi drag.
3. **Database Profil Game (Room DB):**
   - Penyimpanan profil game lokal (SQLite Room) dengan pengaturan sensitivitas spesifik per game (X/Y axis, touch acceleration, sensitivitas scopes, target tier 60/90/120 FPS).
4. **Kalibrasi Sensitivitas & Touch Response:**
   - Pengaturan sensitivitas gyroscope, red dot, scope 2x/4x dengan kurva respons sentuh kustom.
5. **Crosshair Kustom:**
   - Overlay bidikan kustom (Dot, Cross, Circle, Chevron) dengan ukuran, warna, dan ketebalan yang dapat diatur.
6. **Perekam Layar (Game Recorder):**
   - Perekaman gameplay terintegrasi dengan opsi resolusi dan bitrate.

---

## 📲 Cara Instalasi APK

### Melalui Perangkat Android (File Manager)
1. Unduh file `releases/app-debug.apk` atau `releases/app-release.apk` ke ponsel Android Anda.
2. Buka aplikasi **File Manager** / **Downloads** di ponsel Anda.
3. Ketuk file APK tersebut.
4. Jika muncul peringatan *"Install from unknown sources"*, izinkan izin instalasi untuk file manager tersebut.
5. Ketuk **Install** dan buka **ZX Gaming Companion**.

### Melalui Komputer (ADB)
```bash
# Untuk menginstal Debug APK
adb install -r releases/app-debug.apk

# Untuk menginstal Release APK
adb install -r releases/app-release.apk
```

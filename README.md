# WiFi Title Rename

Modul Xposed (LSPosed/Vector) untuk mengganti NAMA TAMPILAN WiFi di HyperOS 2 / Android 15 (Xiaomi) tanpa mengubah SSID asli — hanya hook `WifiEntry.getTitle()`, logika koneksi tidak disentuh.

## Pakai
1. Build debug APK via GitHub Actions atau `./gradlew assembleDebug`.
2. Install APK, aktifkan modul + scope `com.android.settings` (opsional `com.android.systemui`) di LSPosed/Vector, reboot.
3. Buka aplikasinya, tambah mapping: nama WiFi asli (persis seperti tampil) -> nama baru. Berlaku setelah file prefs dibaca ulang (auto-reload saat file berubah).

Catatan: runner CI memakai `app/libs/api-82.jar` (vendor, diambil dari https://api.xposed.info dan diverifikasi), jadi build tidak bergantung pada availability repo maven Xposed.

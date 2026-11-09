# SIMPATIK Posyandu — Aplikasi Android

Aplikasi lapangan untuk kader Posyandu: mendaftarkan kehadiran balita, mengatur antrean layanan, dan mencatat hasil pengukuran saat kegiatan Posyandu berlangsung. Dikembangkan dalam program KKN Politeknik Manufaktur (POLMAN) Bandung 2026 untuk Posyandu Tulip.

Aplikasi ini adalah pendamping **Portal SIMPATIK** (web). Tablet/HP mencatat di lapangan; web mengolah data menjadi KMS, status gizi, dan laporan.

## Fitur

**Antrean dan pendaftaran**
- Daftar sasaran bulanan dari Portal, pencarian nama/NIK, dan pindai kartu QR (kamera depan atau belakang).
- Pendaftaran kehadiran dengan catatan untuk petugas serta pertanyaan YA/TIDAK: ibu masih menyusui (usia 0–23 bulan) dan ASI eksklusif (usia 0–6 bulan).
- Antrean bersama lintas perangkat dengan empat slot panggilan dan panggilan suara. Nomor antrean yang dibatalkan dipakai kembali oleh pendaftar berikutnya tanpa mengubah nomor anak lain.

**Pencatatan pengukuran**
- Form berat badan, panjang/tinggi badan (terlentang/berdiri), LILA, dan LIKA dengan keypad angka besar dan validasi rentang wajar.
- Kolom catatan/pengamatan khusus yang ikut terkirim ke web.
- Riwayat per bulan dan koreksi nilai tanpa membuat catatan ganda; pendataan susulan setelah sesi ditutup.

**Bekerja offline**
- Data disimpan dahulu di SQLite perangkat, lalu dikirim ke REST API saat jaringan tersedia.
- Antrean dan hasil ukur tertunda dapat diteruskan antarperangkat dalam satu Posyandu melalui Nearby Connections.

**Lainnya**
- Penanda kecil di atas layar saat web menjalankan sesi latihan/demo.
- Tata letak menyesuaikan tablet, HP portrait/landscape, dan saat keyboard terbuka.
- Pembaruan aplikasi langsung dari server SIMPATIK: popup pembaruan, unduhan dengan progres, verifikasi berkas, lalu layar pemasangan Android.

## Arsitektur

```
┌────────────── Aplikasi Android ──────────────┐        ┌──── Portal SIMPATIK ────┐
│ WebView (Vue 3)  ── AndroidBridge ──  Kotlin │  HTTPS │ REST API  →  PostgreSQL │
│ index.html          (JS interface)   SQLite, │ ─────▶ │ Web: KMS, status gizi,  │
│ UI & alur kerja                      kamera, │        │ laporan, pengaturan     │
│                                      Nearby, │        └─────────────────────────┘
│                                      updater │
└──────────────────────────────────────────────┘
```

- **Antarmuka**: satu halaman Vue 3 di `app/src/main/assets/index.html`, dimuat WebView.
- **Native**: `MainActivity.kt` menyediakan jembatan `AndroidBridge` (SQLite, discovery server, suara antrean, info versi), `CardScannerActivity.kt` (CameraX + ML Kit), `NearbyClusterManager.kt` (sinkron antarperangkat), dan `AppUpdater.kt` (pembaruan dari server).
- **Server**: aplikasi hanya berkomunikasi dengan REST API Portal menggunakan token sesi pengguna. Tidak ada kredensial database di APK.

## Struktur repo

```
app/
  src/main/
    assets/            index.html (UI Vue), vue.js, jsqr.min.js, logo
    java/.../          MainActivity, AppUpdater, CardScannerActivity, NearbyClusterManager, ui/theme
    res/               ikon, tema, konfigurasi backup, path FileProvider pembaruan
  src/test/            unit test JVM
  src/androidTest/     uji instrumentasi perangkat
  build.gradle.kts     konfigurasi build, versi, dan penandatanganan rilis
docs/
  CHANGELOG.md         riwayat perubahan per versi
  PROGRESS.md          catatan pengerjaan dan verifikasi
  walkthrough/         panduan per fitur/versi
  ui-1.12.3/           tangkapan layar UI
tests/
  app-regression.cjs   uji regresi logika UI (Node.js)
server.properties.example   contoh konfigurasi alamat server untuk build
```

## Membangun aplikasi

Kebutuhan: Android Studio (JDK 17+), Android SDK, dan Node.js untuk uji regresi.

1. Salin `server.properties.example` menjadi `server.properties`, lalu isi alamat REST API Portal SIMPATIK (berakhiran `/api/v1`). Berkas ini tidak masuk repo.
2. Uji dan bangun:

   ```bash
   node tests/app-regression.cjs
   ./gradlew testDebugUnitTest assembleDebug
   ```

3. Untuk APK rilis, siapkan keystore penandatanganan milik tim dan berkas `keystore.properties` di luar repo (path bawaan `~/.simpatik-signing/keystore.properties`, atau env `SIMPATIK_KEYSTORE_PROPERTIES`), lalu jalankan `./gradlew assembleRelease`.

Keystore, kata sandi, dan alamat server **tidak boleh** dimasukkan ke repo; `.gitignore` sudah menolak `*.jks`, `*.keystore`, `keystore.properties`, dan `server.properties`.

## Rilis dan pembaruan

- APK rilis dilampirkan pada halaman [Releases](../../releases).
- Setelah versi 1.15.0 terpasang, pembaruan berikutnya dapat diterbitkan dari server Portal; aplikasi akan menampilkan popup pembaruan. Android tetap meminta konfirmasi pemasangan dan, pada pembaruan pertama, izin "Izinkan dari sumber ini".
- Perpindahan dari versi 1.14.1 ke bawah memerlukan pemasangan ulang satu kali karena nama paket berubah: sinkronkan data di aplikasi lama, pasang versi baru, masuk kembali, lalu hapus aplikasi lama.

## Cabang

- `main` — rilis stabil.
- `development` — pengembangan berikutnya; digabung ke `main` saat rilis.

## Dokumentasi

- [Riwayat perubahan](docs/CHANGELOG.md)
- [Catatan pengerjaan](docs/PROGRESS.md)
- [Panduan per fitur](docs/walkthrough/)
- [Pembagian fitur Android–web](docs/CROSSCHECK-ANDROID-WEB-v1.6.md)

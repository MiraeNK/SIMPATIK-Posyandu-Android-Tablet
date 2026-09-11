# SIMPATIK Posyandu — Aplikasi Tablet Android

Aplikasi Android untuk pencatatan pengukuran balita saat kegiatan Posyandu. Versi 1.3 memusatkan tablet pada pekerjaan lapangan, sedangkan KMS lengkap, analitik gizi, laporan, pengelolaan master data, dan koreksi setelah kegiatan menjadi tanggung jawab Portal SIMPATIK.

## Ruang lingkup tablet

- Login kader menggunakan autentikasi server.
- Mengunduh dan menyimpan daftar balita untuk pencarian offline.
- Mencari balita berdasarkan nama atau NIK.
- Mengisi tanggal, BB, PB/TB, LILA, LIKA, dan cara ukur.
- Memeriksa kelengkapan serta rentang angka sebelum menyimpan.
- Menyimpan pengukuran lebih dahulu ke SQLite Android.
- Mencegah pencatatan ganda untuk anak dan periode yang sama.
- Memperbaiki data bulan berjalan dengan membuka kembali anak yang sama.
- Mengantrekan dan mencoba kembali sinkronisasi ketika jaringan tersedia.
- Menampilkan progres operasional bulan berjalan dan jumlah antrean sinkron.

## Batas dengan Portal SIMPATIK

Fitur berikut dikelola di website Portal:

- KMS dan riwayat pengukuran lengkap.
- Perhitungan serta penetapan status gizi resmi.
- Dashboard analitik dan pemantauan kelompok.
- Laporan dan ekspor.
- Pendaftaran serta pengelolaan data induk anak.
- Koreksi setelah kegiatan dan audit perubahan.
- Pengaturan akun, rumus, serta integrasi database.

Tablet hanya menampilkan pengukuran sebelumnya secara ringkas untuk membantu kader memeriksa kewajaran input. Tablet tidak menghitung kategori gizi resmi.

## Alur data

1. Kader memilih balita dan mengisi pengukuran.
2. Data divalidasi lalu disimpan atomik di SQLite Android.
3. Satu anak hanya memiliki satu catatan untuk satu periode; penyimpanan ulang memperbarui catatan yang sama.
4. Data masuk antrean sinkronisasi.
5. Saat jaringan tersedia, aplikasi melakukan upsert ke server.
6. Website mengolah data menjadi status gizi, KMS, analitik, dan laporan.

## Build dan pengujian

Kebutuhan: Android Studio/JDK 11+, Android SDK, dan Gradle Wrapper yang tersedia di repositori.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug --console=plain
node .\tests\app-regression.cjs
```

APK debug dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`.

## Dokumentasi

- `CHANGELOG.md` — riwayat perubahan per versi.
- `PROGRESS.md` — posisi pekerjaan, hasil verifikasi, dan pekerjaan lanjutan.
- `WALKTHROUGH-ANDROID-FOKUS-INPUT-v1.3.md` — alur penggunaan untuk kader.
- `TESTING-2026-09-10.md` — laporan pengujian versi 1.2.

## Cabang pengembangan

Versi fokus input dikembangkan pada branch `codex/fokus-input-tablet`. Branch `main` tetap menyimpan versi 1.2 dengan fitur lengkap sebagai arsip yang dapat digunakan kembali.

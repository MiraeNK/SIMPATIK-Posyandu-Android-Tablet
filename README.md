# SIMPATIK Posyandu — Aplikasi Tablet Android

Aplikasi Android untuk pencatatan operasional saat kegiatan Posyandu. Versi 1.6.2 mencakup pemindaian kartu sasaran, skrining awal, antrean layanan harian, pencarian anak yang mudah dibaca, pendaftaran anak, pengukuran, riwayat bulanan, koreksi nilai, dan sinkronisasi offline. KMS lengkap, analitik gizi, dan laporan tetap menjadi tanggung jawab Portal SIMPATIK.

## Ruang lingkup tablet

- Login kader menggunakan autentikasi server.
- Mengunduh dan menyimpan daftar balita untuk pencarian offline.
- Mencari balita berdasarkan nama atau NIK.
- Mendaftarkan balita baru dan mencegah NIK ganda.
- Mendaftarkan kehadiran anak ke antrean harian dan mencegah antrean ganda.
- Memindai QR kartu sasaran dari Portal SIMPATIK memakai kamera perangkat.
- Menampilkan kekurangan identitas, Buku KIA, IMD, dan imunisasi sebelum check-in.
- Memanggil, melewati, membatalkan, dan menyelesaikan giliran dari menu antrean.
- Memisahkan antrean layanan dari formulir pencatatan pengukuran.
- Menyimpan antrean ke SQLite Android agar tetap tersedia setelah aplikasi ditutup.
- Mengisi tanggal, BB, PB/TB, LILA, LIKA, dan cara ukur.
- Memeriksa kelengkapan serta rentang angka sebelum menyimpan.
- Menyimpan pengukuran lebih dahulu ke SQLite Android.
- Mencegah pencatatan ganda untuk anak dan periode yang sama.
- Melihat riwayat pencatatan melalui tab bulan dan tahun.
- Memperbaiki nilai pengukuran dari riwayat tanpa membuat entri ganda.
- Mengantrekan dan mencoba kembali sinkronisasi ketika jaringan tersedia.
- Menampilkan progres operasional bulan berjalan dan jumlah antrean sinkron.

## Batas dengan Portal SIMPATIK

Fitur berikut dikelola di website Portal:

- KMS dan visualisasi pertumbuhan lengkap.
- Perhitungan serta penetapan status gizi resmi.
- Dashboard analitik dan pemantauan kelompok.
- Laporan dan ekspor.
- Pengelolaan lanjutan dan penggabungan data induk anak.
- Audit perubahan tingkat administrator.
- Pengaturan akun, rumus, serta integrasi database.

Tablet hanya menampilkan pengukuran sebelumnya secara ringkas untuk membantu kader memeriksa kewajaran input. Tablet tidak menghitung kategori gizi resmi.

## Alur data

1. Kader menambahkan anak yang hadir, memanggil, dan menyelesaikan gilirannya melalui **Antrean Hari Ini**.
2. Kader kembali ke beranda lalu membuka **Pencatatan Langsung** untuk memilih anak dan memasukkan hasil ukur.
3. Data divalidasi lalu disimpan atomik di SQLite Android.
4. Satu anak hanya memiliki satu catatan untuk satu periode; penyimpanan ulang memperbarui catatan yang sama.
5. Data masuk antrean sinkronisasi.
6. Saat jaringan tersedia, aplikasi melakukan upsert ke server.
7. Website mengolah data menjadi status gizi, KMS, analitik, dan laporan.

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
- `WALKTHROUGH-ANDROID-ANTREAN-v1.5.md` — alur antrean dan pengukuran terbaru untuk kader.
- `WALKTHROUGH-KARTU-ANTREAN-v1.6.md` — alur kartu, skrining, antrean, dan ulang ukur.
- `CROSSCHECK-ANDROID-WEB-v1.6.md` — pembagian fitur dan status koneksi Android–web.
- `TESTING-2026-09-10.md` — laporan pengujian versi 1.2.

## Cabang pengembangan

Versi fokus input dikembangkan pada branch `codex/fokus-input-tablet`. Branch `main` tetap menyimpan versi 1.2 dengan fitur lengkap sebagai arsip yang dapat digunakan kembali.

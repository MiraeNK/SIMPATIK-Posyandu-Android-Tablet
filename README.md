# SIMPATIK Posyandu — Aplikasi Tablet Android

Aplikasi Android untuk pencatatan operasional saat kegiatan Posyandu. Versi 1.10.8 memisahkan batas kegagalan login antarperangkat, membedakan jenis pesan kesalahan login, dan menata ulang halaman utama, antrean, pencatatan, riwayat, serta pendaftaran untuk layar HP. Versi sebelumnya menambahkan pendataan susulan setelah sesi ditutup; nilai LILA/LIKA bulan sebelumnya juga ditampilkan sebagai konteks pada form bulan berikutnya. Aplikasi memakai static domain ngrok, memeriksa server dan keabsahan sesi, serta menentukan hak akses otomatis dari akun yang berhasil login. Aplikasi tetap mendukung daftar sasaran bulanan, pemindaian kartu, skrining awal, antrean layanan, pencatatan, riwayat, koreksi nilai, serta sinkronisasi offline. KMS lengkap, analitik gizi, dan laporan tetap menjadi tanggung jawab Portal SIMPATIK.

Versi 1.10 berkomunikasi hanya dengan REST API Portal SIMPATIK. REST API yang
memegang koneksi PostgreSQL Supabase; APK tidak menyimpan URL proyek, publishable
key, secret key, maupun kredensial database.

## Ruang lingkup tablet

- Login kader menggunakan autentikasi server.
- Memeriksa koneksi domain ngrok tetap pada splash screen bila sesi login tersimpan.
- Tetap membuka data lokal dengan pemberitahuan yang jelas saat server belum menyala.
- Mengunduh dan menyimpan daftar sasaran periode aktif untuk pencarian offline.
- Mencari balita berdasarkan nama atau NIK.
- Mendaftarkan balita baru dan mencegah NIK ganda.
- Mendaftarkan kehadiran anak ke antrean harian dan mencegah antrean ganda.
- Memindai QR kartu sasaran dari Portal SIMPATIK memakai kamera perangkat.
- Menampilkan kekurangan identitas, Buku KIA, IMD, dan imunisasi sebelum check-in.
- Menyimpan catatan opsional untuk setiap anak saat masuk antrean.
- Memanggil, melewati, dan membatalkan giliran dari menu antrean.
- Memisahkan antrean layanan dari formulir pencatatan pengukuran.
- Menampilkan pilihan **Dari Antrean** dan **Semua Balita** di menu Pencatatan.
- Menandai anak yang dipanggil sebagai **Dalam Proses** dan menyelesaikan antrean setelah hasil ukur tersimpan.
- Menyimpan antrean ke SQLite Android agar tetap tersedia setelah aplikasi ditutup.
- Mengisi tanggal, BB, PB/TB, LILA, LIKA, dan cara ukur.
- Memeriksa kelengkapan serta rentang angka sebelum menyimpan.
- Menyimpan pengukuran lebih dahulu ke SQLite Android.
- Mencegah pencatatan ganda untuk anak dan periode yang sama.
- Melihat riwayat pencatatan melalui tab bulan dan tahun.
- Memperbaiki nilai pengukuran dari riwayat tanpa membuat entri ganda.
- Mengantrekan dan mencoba kembali sinkronisasi ketika jaringan tersedia.
- Menampilkan progres operasional bulan berjalan dan jumlah antrean sinkron.
- Menandai sasaran yang belum dilayani sebagai **Tidak hadir** melalui konfirmasi beres sesi tanpa menghapus data induk anak.
- Setelah sesi ditutup, membuka tab **Susulan di Luar Sesi** pada menu Pencatatan untuk mengukur anak tidak hadir secara manual.
- Menetapkan tanggal ukur susulan ke periode sasaran, menyimpan offline dahulu, lalu menandai **Pendataan susulan tersimpan** hanya setelah API berhasil menyimpan.
- Mengisi LILA/LIKA dari pengukuran sebelumnya sebagai konteks. Kader tetap diminta mengukur ulang setelah interval tiga bulan.

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

1. Admin Portal mengimpor atau mengganti daftar sasaran periode; tablet mengambil daftar tersebut saat sinkronisasi.
2. Kader memindai kartu sasaran atau mencari anak dari daftar periode yang diterbitkan Portal, lalu menambahkan catatan operasional saat anak masuk **Antrean Hari Ini**.
3. Kader menekan **Panggil** sehingga status anak menjadi **Dalam Proses**.
4. Petugas pencatatan membuka **Pencatatan Langsung**; tab **Dari Antrean** tampil pertama dan membawa catatan pendaftaran.
5. Data divalidasi lalu disimpan atomik di SQLite Android; antrean anak otomatis menjadi **Selesai**.
6. Satu anak hanya memiliki satu catatan untuk satu periode; penyimpanan ulang memperbarui catatan yang sama.
7. Data masuk antrean sinkronisasi lalu dikirim ke REST API Portal saat jaringan tersedia.
8. Setelah layanan berakhir, **Konfirmasi beres sesi** menandai sasaran tersisa sebagai tidak hadir.
9. Website mengolah data menjadi status gizi, KMS, analitik, dan laporan.

## Build dan pengujian

Kebutuhan: Android Studio/JDK 11+, Android SDK, dan Gradle Wrapper yang tersedia di repositori.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug --console=plain
node .\tests\app-regression.cjs
```

APK debug dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`.

### Koneksi website melalui ngrok

1. Siapkan authtoken dan static domain pada `.env.ngrok` di repo Portal.
2. Pastikan `PORTAL_API_BAWAAN` memakai static domain yang sama dengan akhiran
   `/api/v1`.
3. Klik dua kali `mulai-ngrok.bat` pada repo Portal dan biarkan jendela aktif.
4. Buka aplikasi. Bila sesi login tersimpan, splash menampilkan
   **Menyambungkan ke server…** sampai pemeriksaan selesai.
5. Masuk dengan akun live, lakukan sinkronisasi, kemudian pindai QR kartu yang
   dicetak dari menu **Kartu Balita** di Portal.

Jika server belum menyala, pengguna yang pernah login tetap masuk ke aplikasi
dan mendapat pemberitahuan bahwa data akan disimpan di Android. Pengguna yang
belum pernah login langsung melihat halaman login yang bersih tanpa pengaturan
koneksi teknis.

## Dokumentasi

- `CHANGELOG.md` — riwayat perubahan per versi.
- `PROGRESS.md` — posisi pekerjaan, hasil verifikasi, dan pekerjaan lanjutan.
- `WALKTHROUGH-ANDROID-ANTREAN-v1.5.md` — alur antrean dan pengukuran terbaru untuk kader.
- `WALKTHROUGH-KARTU-ANTREAN-v1.6.md` — alur kartu, skrining, antrean, dan ulang ukur.
- `CROSSCHECK-ANDROID-WEB-v1.6.md` — pembagian fitur dan status koneksi Android–web.
- `TESTING-2026-09-10.md` — laporan pengujian versi 1.2.

## Cabang pengembangan

Versi fokus input dikembangkan pada branch `codex/fokus-input-tablet`. Branch `main` tetap menyimpan versi 1.2 dengan fitur lengkap sebagai arsip yang dapat digunakan kembali.

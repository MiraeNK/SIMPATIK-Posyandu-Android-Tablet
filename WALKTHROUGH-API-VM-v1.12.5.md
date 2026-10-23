# Android 1.12.5 — API pada VM

Tanggal: 23 Oktober 2026.

- Website: https://tile-hopes-dec-wisconsin.trycloudflare.com/
- REST API: https://tile-hopes-dec-wisconsin.trycloudflare.com/api/v1
- Host aplikasi: ubuntu-headless-daffa, SSH hafizh@100.127.247.109.
- Database tetap Supabase yang sudah dipakai, akun/kata sandi tidak diubah.

## Memasang pembaruan

Pasang APK versi 1.12.5 (versionCode 29) sebagai pembaruan aplikasi lama. Jangan uninstall atau hapus data. Pada pembukaan pertama, konfigurasi alamat lama dipindahkan ke VM; sesi, cache anak dan pekerjaan offline dipertahankan. Pengaturan yang diganti secara manual sesudah migrasi tetap dipertahankan pada pembukaan berikutnya.

Buka aplikasi, tunggu splash, lalu login bila sesi sudah kedaluwarsa. Status koneksi harus menunjukkan Server online. Native discovery dan WebView memakai hostname VM yang sama. Data baru tetap masuk ke database yang sama melalui API.

## Menjalankan server

Laptop tidak perlu menjalankan web/ngrok. VM harus hidup dan terhubung internet. Web dan API dilayani systemd; konfigurasi serta perintah operasional tercatat di repo web dalam docs/deployment-vm-2026-10-23.md.

Cloudflare Quick Tunnel bersifat sementara. Restart API saja mempertahankan tunnel yang berjalan; restart proses tunnel/VM dapat menghasilkan URL baru. Jika URL berubah, perbarui alamat pada Pengaturan koneksi aplikasi atau distribusikan APK dengan endpoint baru. URL permanen memerlukan named tunnel/domain atau Funnel yang diaktifkan pemilik tailnet.

## Verifikasi

- HTTPS publik, health database dan penolakan akses tanpa login (401) lulus.
- Login Mirae dan Yuliani serta baca 92 sasaran lulus; sesi pengujian dicabut sesudahnya.
- Cakupan Mirae global, Yuliani hanya Posyandu Tulip RW 18, sesuai hasil API.
- Tes migrasi alamat mempertahankan sesi/data offline; tes lain memastikan hostname native dan WebView sama.
- HP fisik belum terdeteksi ADB ketika pemeriksaan deployment. Pengujian UI fisik versi ini belum dilakukan.

Hasil build akhir: 43 tes regresi JavaScript lulus; Gradle testDebugUnitTest, lintDebug dan assembleDebug lulus. APK SIMPATIK-1.12.5-debug.apk, SHA-256 58294c5d2a1c39ffa4d555beff29fa0a2bc5488f63ea8138f5f1fdb48d66f7ee.

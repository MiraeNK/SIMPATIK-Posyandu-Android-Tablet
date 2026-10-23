# Koneksi dan Status Perangkat — 1.12.4

Pembaruan 21 Oktober 2026. APK versionCode 28, versionName 1.12.4.

## Perbaikan koneksi

HP menggunakan API `https://pediatric-lung-hiking-pills.trycloudflare.com/api/v1`.
Versi lama menulis mode `lokal` pada setiap hasil penemuan server, termasuk ketika native Android menemukan Cloudflare. Versi ini menentukan label dari URL efektif dan memakai label yang sama pada pemberitahuan.

Pada HP yang diuji, beranda kini menampilkan **Server online · API & database siap**. Ini terpisah dari status Nearby; akun super admin lintas wilayah masih memerlukan konteks satu Posyandu untuk membentuk cluster Nearby.

## Daftar perangkat di web

1. Masuk ke web dan buka tombol status perangkat di bawah menu.
2. Akun kader/admin Posyandu hanya menerima daftar wilayah akun tersebut dari server. Penugasan RT tidak membatasi perangkat lain dalam Posyandu yang sama.
3. Super admin dapat melihat seluruh Posyandu, termasuk perangkat milik akun lintas wilayah.
4. Daftar menampilkan model/nama perangkat, nama akun, potongan ID instalasi, Posyandu/RW, versi aplikasi, jalur koneksi, dan waktu laporan terakhir.
5. Gunakan pencarian perangkat/akun/RW atau filter Online, Siaga, dan Offline.

Laporan dikirim sekitar setiap 15 detik. Status **Online** berarti laporan diterima dalam 45 detik terakhir; **Siaga** sampai 2 menit; **Offline** lebih dari 2 menit. Catatan offline tetap disimpan di database, termasuk setelah server dimulai ulang.

Web tidak dapat mengetahui aktivitas baru perangkat yang sama sekali belum berhasil mengirim laporan. Data terakhir bukan jaminan kondisi terbaru saat perangkat kehilangan jaringan. Jika API web tidak terjangkau, panel menandai status sebagai belum diketahui dan mempertahankan laporan terakhir yang sudah dimuat.

Browser web yang sudah login juga melapor. Dua browser pada komputer yang sama dapat tercatat sebagai dua instalasi browser; potongan ID membantu membedakannya.

## Pemasangan

Pasang `app/build/outputs/apk/debug/SIMPATIK-1.12.4-debug.apk` sebagai pembaruan pada tablet/HP lain. Jangan hapus aplikasi atau bersihkan penyimpanannya untuk memasang pembaruan.

Endpoint heartbeat memerlukan sesi yang sah. Versi lama yang mengirim laporan tanpa token tidak akan muncul sampai aplikasinya diperbarui. Data anak dan akun tidak diubah oleh migrasi status perangkat.

## Verifikasi

- 41 tes regresi Android JavaScript lulus dan build APK berhasil.
- Samsung SM-S711B Android 16: discovery native menemukan Cloudflare, label online benar, heartbeat diterima, dan versi 1.12.4 terlihat pada API web.
- Endpoint perangkat menolak permintaan tanpa sesi dengan HTTP 401.
- 8 tes server pada skema PostgreSQL sementara membuktikan isolasi dua RW, pemalsuan wilayah diabaikan, super admin melihat seluruh perangkat, upsert tanpa duplikasi, dan catatan offline bertahan setelah restart aplikasi server.
- Pemeriksaan tipe client/server dan lint komponen perangkat lulus.
- Panel web mobile memakai dialog native agar berada di atas laci menu, dengan pencarian dan filter yang tetap dapat dioperasikan.

APK: 13.991.044 byte.

SHA-256: `75a8ed3d908c4d363678d7734d2602b7ddcc4a6538b1eb04ec87a880377ca9d0`.

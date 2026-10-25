# Progress Pengembangan

Terakhir diperbarui: 21 Oktober 2026

### 1.12.4 — Koneksi online dan status perangkat per Posyandu

- [x] Menghapus penandaan lokal secara paksa pada hasil discovery URL Cloudflare.
- [x] Memperbaiki laporan perangkat yang sebelumnya tidak menyertakan token login.
- [x] Menambahkan model perangkat dari native Android dan jalur koneksi pada heartbeat.
- [x] Memasang pembaruan 1.12.4 di HP tanpa membersihkan data/sesi.
- [x] Menguji label **Server online · API & database siap**, discovery native, heartbeat HTTP 200, dan daftar perangkat tanpa sesi HTTP 401.
- [x] Menjalankan 41 tes regresi dan `assembleDebug`; lulus.
- [x] Memasang migrasi web 022 untuk status perangkat yang bertahan saat offline dan restart server.
- [x] Menguji pemisahan RW, penolakan klaim wilayah palsu, cakupan super admin, dan persistensi melalui 8 tes API/database pada skema uji terpisah.

APK: `app/build/outputs/apk/debug/SIMPATIK-1.12.4-debug.apk`.

SHA-256: `75a8ed3d908c4d363678d7734d2602b7ddcc4a6538b1eb04ec87a880377ca9d0`.

Panduan: `WALKTHROUGH-PERANGKAT-v1.12.4.md`.

### 1.12.3 — UI HP dan popup catatan scan

- [x] Menemukan penyebab layar kosong langsung pada Samsung SM-S711B Android 16: tinggi CSS viewport bernilai nol; kontainer kini mengikuti tinggi visual viewport yang terukur.
- [x] Memasang APK sebagai pembaruan di HP; data lokal dan sesi login dipertahankan.
- [x] Merapikan ikon, tombol navigasi, padding, header pendaftaran, serta dua pintasan sejajar dengan Daftarkan Balita selebar satu baris.
- [x] Menguji Lihat/Sembunyikan password dan form login saat keyboard terbuka.
- [x] Menguji popup hasil scan: simpan, lewati, batal, dan Kembali Android tanpa membuat antrean uji di database sungguhan.
- [x] Menguji beranda, pendaftaran, daftar pencatatan, daftar riwayat, pemulihan sesi setelah restart, dan rotasi HP.
- [x] Build `assembleDebug` berhasil dan 38 tes regresi JavaScript lulus.
- [ ] Uji kamera dengan kartu cetak di perangkat: hasil decode disimulasikan pada uji UI ini.
- [ ] Verifikasi visual final tablet belum tuntas karena ANR System UI dan launch emulator macet; perlu diuji ulang pada tablet fisik atau emulator yang stabil.
- [ ] Akun Mirae belum memperoleh konteks Nearby: API `/cluster-context` mengembalikan 409 karena akun belum terikat satu Posyandu. Akses super admin tidak diubah.

APK: `app/build/outputs/apk/debug/SIMPATIK-1.12.3-debug.apk`.

Walkthrough dan batas pengujian: `WALKTHROUGH-UI-HP-v1.12.3.md`.

### 1.10.8 — Login multi-tablet dan layout HP

- [x] Membatasi percobaan salah per identitas instalasi, bukan berbagi ambang rendah antarsemua tablet di alamat jaringan yang sama.
- [x] Menambahkan batas jaringan cadangan yang lebih longgar untuk mencegah percobaan login tersebar tanpa mengunci tablet hanya karena satu perangkat salah ketik.
- [x] Mempertahankan spasi kata sandi dan menampilkan pesan sesuai status HTTP login.
- [x] Menambahkan layout layar kecil untuk antrean, daftar pencatatan, riwayat, pendaftaran, dan halaman login.
- [x] Menjalankan 32 pengujian regresi; seluruhnya lulus.
- [x] Menjalankan pemeriksaan tipe API server; lulus.
- [x] Menjalankan `assembleDebug`; build APK 1.10.8 berhasil.

### 1.10.7 — Pendataan susulan setelah sesi ditutup

- [x] Menambahkan tab pencatatan khusus untuk sasaran tidak hadir setelah sesi ditutup.
- [x] Memungkinkan kader mengisi pengukuran secara manual pada periode sasaran semula.
- [x] Menunjukkan status menunggu sinkron saat data baru aman di perangkat.
- [x] Menulis penanda pendataan susulan ke catatan sasaran bersama pengukuran dalam transaksi server.
- [x] Menampilkan status tersimpan setelah server mengonfirmasi data.
- [x] Mengisi LILA/LIKA dari bulan sebelumnya tanpa menyimpan ulang nilai lama sebagai pengukuran baru.
- [x] Menjalankan 31 pengujian regresi; seluruhnya lulus.
- [x] Menjalankan pemeriksaan tipe API server; lulus.
- [x] Menjalankan `assembleDebug`; build berhasil.

APK debug: `app/build/outputs/apk/debug/app-debug.apk`

SHA-256: `C42173B7C091A531ADB31397031EF92AC7F4D251EB3E1F23BA747D0CC341BE67`

Ukuran: 13.882.017 byte.

### Migrasi Supabase versi 1.8.0

- [x] Mengganti URL proyek lama dengan proyek KKN POSYANDU TULIP.
- [x] Memakai publishable key pada APK; secret key tetap hanya untuk proses server.
- [x] Mengirim token login pengguna pada seluruh operasi data cloud.
- [x] Mengubah pendaftaran dan pengukuran menjadi RPC atomik yang memakai skema utama Portal.
- [x] Menjalankan 7 migrasi cloud dan mengisi 906 baris standar WHO-2006.
- [x] Memverifikasi RLS pada 7 tabel domain serta 2 view dan 2 RPC tablet.
- [ ] Membuat akun Supabase Auth kader/admin dan menguji alur login langsung.
- [x] Menjalankan 18 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.8.0_supabase-kkn.apk`

SHA-256: `E61611914714D7D0D82436E447F6D586207F2DF7A880B9D7A437D2BE8D256445`

Ukuran: 13.822.495 byte.

### Verifikasi versi 1.7.1

- [x] Merapikan tata letak formulir pengukuran setelah penambahan catatan antrean.
- [x] Menampilkan BB, TB/PB, LILA, dan LIKA secara utuh pada layar tablet.
- [x] Menyusun LILA dan LIKA dalam dua kartu sejajar.
- [x] Memadatkan keypad, panel catatan, dan footer tanpa mengecilkan target sentuh utama.
- [x] Menjalankan 18 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.
- [x] Uji Pixel Tablet: seluruh kolom pengukuran, keypad, catatan, dan tombol Simpan tampil utuh tanpa tumpang tindih.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.7.1_form-pengukuran-rapi.apk`

SHA-256: `B60D59CD08D2EEF1267E6D994ED9C86721694659B8B03933AE22F3672701D5A5`

Ukuran: 13.822.563 byte.

### Verifikasi versi 1.7.0

- [x] Menambahkan tab Dari Antrean sebagai daftar awal pada Pencatatan.
- [x] Menambahkan tab Semua Balita untuk pencatatan di luar antrean.
- [x] Menampilkan status Dalam Proses untuk anak yang telah dipanggil.
- [x] Membatasi pembukaan formulir antrean hanya untuk anak yang sudah dipanggil.
- [x] Menyimpan dan menampilkan catatan pendaftaran pada seluruh alur pelayanan.
- [x] Menyelesaikan antrean setelah penyimpanan hasil ukur berhasil.
- [x] Menjalankan 18 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.
- [x] Uji Pixel Tablet: dua tab Pencatatan, status Dalam Proses, dan panel catatan pada formulir tampil dengan benar.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.7.0_pencatatan-antrean.apk`

SHA-256: `22239BD75E715B70343048E303B3FA3A2F75F86DCB88DB9708DA5E04F31BE38B`

Ukuran: 13.857.202 byte.

### Verifikasi versi 1.6.2

- [x] Memisahkan antrean layanan dari formulir pencatatan.
- [x] Menghapus aksi Ukur dan Mulai Pengukuran dari halaman antrean.
- [x] Menyediakan aksi Tandai Selesai untuk menutup giliran layanan.
- [x] Menjaga pemilihan anak dan seluruh input hasil ukur di menu Pencatatan Langsung.
- [x] Menjalankan 18 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.
- [x] Uji Pixel Tablet: beranda membedakan fungsi Antrean dan Pencatatan, serta Pencatatan Langsung membuka daftar pemilihan balita.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.6.2_pemisahan-antrean-pencatatan.apk`

SHA-256: `28501B3187449777A005CCD35A8A91480070F6512326B840A3F41470ACBFA28A`

Ukuran: 13.820.947 byte.

### Verifikasi versi 1.6.1

- [x] Memindahkan pencarian manual dari panel kanan ke tab khusus selebar layar.
- [x] Memperbesar target sentuh, teks hasil, kolom pencarian, dan tombol tambah.
- [x] Membatasi hasil hingga pengguna mengetik sedikitnya dua karakter.
- [x] Menjaga scan kamera dan skrining pada tab Tambah Anak yang sama.
- [x] Menjalankan 18 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.
- [x] Uji Pixel Tablet: perpindahan dua tab, pencarian nama, hasil pencarian, dan penambahan anak ke antrean berjalan baik.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.6.1_pencarian-mudah.apk`

SHA-256: `B3B9F4CF7D006AD703EF8B051D3104C82FECB634E837745698D894A8652164E0`

Ukuran: 13.821.259 byte.

### Verifikasi versi 1.6

- [x] Menambahkan scan QR kartu sasaran dengan kamera perangkat tanpa meminta izin kamera langsung pada aplikasi.
- [x] Menyamakan payload QR dengan kartu yang diterbitkan Portal SIMPATIK.
- [x] Menampilkan skrining awal sebelum konfirmasi antrean.
- [x] Menolak hasil scan yang tidak ditemukan atau tidak unik.
- [x] Menambahkan peringatan ulang pengukuran berdasarkan catatan sebelumnya.
- [x] Menjalankan 17 pengujian regresi JavaScript; seluruhnya lulus.
- [x] Menjalankan unit test Gradle, Android lint, dan assembleDebug; seluruhnya lulus.
- [x] Uji Pixel Tablet: beranda, halaman antrean, pembukaan kamera Google Code Scanner, dan panel hasil skrining.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.6_scan-skrining.apk`

## Selesai

- [x] Membuat branch `codex/fokus-input-tablet` dari versi terbaru `main` (`7ffbc19`).
- [x] Menjaga branch `main` tanpa perubahan.
- [x] Menetapkan batas tanggung jawab Android dan Portal.
- [x] Merampingkan navigasi Android menjadi login → beranda → daftar balita → formulir.
- [x] Menghapus akses pengguna ke KMS, analitik gizi, rumus, laporan, master data, dan pengaturan database.
- [x] Menghapus kalkulasi status gizi contoh dari alur aplikasi.
- [x] Menambahkan progres pencatatan bulan berjalan.
- [x] Menambahkan tombol sinkronisasi manual dan indikator antrean.
- [x] Mempertahankan SQLite, validasi, koreksi periode aktif, upsert, dan retry offline.
- [x] Memperbaiki tanggal lokal perangkat.
- [x] Memperbarui README, changelog, dan walkthrough versi 1.3.
- [x] Mengembalikan riwayat baca-saja dengan tab bulan dan pilihan tahun.
- [x] Mengaktifkan koreksi nilai dari riwayat Android.
- [x] Mengembalikan pendaftaran balita baru pada beranda.
- [x] Menyimpan pendaftaran ke Android sebelum upsert server.
- [x] Menambahkan identitas POLMAN Bandung dan hak cipta 2026 pada splash screen.
- [x] Menambahkan antrean layanan harian sebagai alur utama Android.
- [x] Mencegah antrean ganda untuk anak dan tanggal yang sama.
- [x] Menambahkan panggil, lewati, batal, dan penyelesaian giliran pada antrean.
- [x] Menyimpan dan memulihkan antrean melalui SQLite Android.
- [x] Menambahkan migrasi database v1 ke v2 tanpa menghapus data lama.
- [x] Menyesuaikan login, antrean, dan formulir pengukuran untuk ponsel portrait.

## Verifikasi

- [x] Regression test Node.js versi 1.4: 14/14 lulus.
- [x] Gradle unit test versi 1.4: `testDebugUnitTest` lulus.
- [x] Build APK debug versi 1.4: `assembleDebug` lulus.
- [x] Pemeriksaan metadata APK versi 1.4: versionCode 4, versionName 1.4, minSdk 24, targetSdk 37.
- [x] Uji pada emulator Pixel Tablet API 33: splash, beranda, riwayat bulanan, dan pendaftaran tampil dengan benar.

### Verifikasi versi 1.5

- [x] Regression test Node.js: 15/15 lulus.
- [x] Gradle unit test dan kompilasi Kotlin lulus.
- [x] Build APK debug versionCode 5, versionName 1.5.
- [x] Uji Pixel Tablet: beranda, antrean kosong, tambah dua anak, panggil, dan pemulihan SQLite setelah force-stop.
- [x] Uji viewport ponsel portrait 1080 × 2400: login, antrean, formulir ukur, simpan, dan penyelesaian antrean.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.5_antrean.apk`

SHA-256: `F62B831AFDCCCFA11333AEB00D49459B11BC3A8FC82AAC98B04C5E59CF15F034`

Ukuran: 12.849.198 byte. Aset `index.html` di dalam APK sama dengan sumber saat build.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.4_operasional.apk`

SHA-256: `6A2F06AF5C8E63F317D0B521FC1851A5A4E7B885B37EB57924F66A240E8798ED`

Ukuran: 12.838.983 byte. Aset `index.html` di dalam APK sama dengan sumber saat build.

## Integrasi Portal yang masih diperlukan

- [ ] Tetapkan endpoint sinkronisasi resmi Portal sebagai pengganti kontrak Supabase sementara.
- [ ] Selaraskan identitas anak menggunakan `anak_id` server; NIK tetap sebagai data pencarian, bukan primary key.
- [ ] Selaraskan nama field dan enum pengukuran dengan model Portal.
- [ ] Tambahkan autentikasi perangkat dan idempotency key di endpoint Portal.
- [ ] Portal menghitung status gizi setelah menerima pengukuran dan menyimpan audit koreksi.
- [ ] Tetapkan aturan penutupan periode: tablet mengoreksi periode aktif, Portal mengoreksi periode yang sudah ditutup.

Dokumen ini harus diperbarui setiap kali satu tahap verifikasi atau integrasi selesai.

## 23 Oktober 2026 — Persiapan pemindahan API ke VM

- Menyimpan seluruh pembaruan Android 1.12.4 ke branch codex/fokus-input-tablet sebelum perubahan endpoint VM.
- 41 tes regresi Android lulus. Endpoint baru dan APK dicatat setelah web/API di VM lolos pengujian.
- Riwayat lokal, data anak, akun dan kata sandi dipertahankan.

### API VM — 1.12.5

- Web/API VM lulus health Supabase, login dan pembatasan akses publik.
- Endpoint native dan WebView diarahkan ke tile-hopes-dec-wisconsin.trycloudflare.com.
- Migrasi konfigurasi satu kali memindahkan alamat laptop ke VM tanpa menghapus sesi atau data offline.
- Lihat WALKTHROUGH-API-VM-v1.12.5.md untuk pemasangan dan batas URL sementara.

- Build akhir 1.12.5: 43 tes JavaScript lulus; unit test, lint dan assembleDebug lulus. SHA-256 APK: 58294c5d2a1c39ffa4d555beff29fa0a2bc5488f63ea8138f5f1fdb48d66f7ee.

### 1.12.6 — Pemulihan unggahan KMS dan daftar anak (23 Oktober 2026)

- Unggah ulang berjalan setiap siklus koneksi sehat, bukan hanya transisi offline ke online. Timeout 12 detik mencakup pembacaan respons; permintaan gagal tetap dalam SQLite.
- Satu pengiriman batch pada satu waktu. Respons kiriman lama tidak menghapus koreksi yang lebih baru.
- Snapshot Nearby dikirim berkala dan saat peer tersambung; penerima bisa mengunggah antrean lalu membagikan konfirmasi dengan payload yang cocok. Workspace/RW tetap diverifikasi.
- Daftar anak ditarik berkala dan setelah login. Pendaftaran serta riwayat pending dipertahankan saat penggabungan cache. Paket server mencakup anak baru di Posyandu walau belum menjadi sasaran bulan aktif; status sasaran tidak dibuat otomatis.
- Label KMS membedakan tersimpan lokal/menunggu unggahan.
- Pengujian: 49 regresi JS lulus, termasuk relay dua device simulasi, retry pada koneksi sehat, refresh cache, dan koreksi saat request berlangsung. Uji HTTP/database terisolasi 14 lulus. HP/tablet belum terdeteksi melalui ADB saat pengecekan.

- Tambahan: identitas SQLite dan relay anak tanpa NIK menggunakan ANAK-ID server, bukan nilai null bersama.

### 1.12.7 — Batas sasaran bulanan (23 Oktober 2026)

- Memperbaiki regresi 1.12.6 yang menghitung seluruh master anak sebagai sasaran.
- Total sasaran, belum dilayani, susulan, pencarian antrean dan scan kartu wajib memakai idSasaran yang sah. Status sasaran kosong tidak lagi otomatis menjadi menunggu.
- Master tetap tersedia pada riwayat/semua balita. Pendaftaran baru tidak otomatis masuk sasaran; admin menerbitkannya dari web.
- API kompatibel perangkat lama: anak hanya sasaran, masterAnak terpisah. Tidak mengubah daftar sasaran, pengukuran, atau antrean di database.
- Pengujian: 52 regresi JS termasuk kasus 92 sasaran (86 menunggu, 5 selesai, 1 tidak hadir) dengan master tambahan; 14 tes HTTP/database terisolasi.

## 2026-10-23 — v1.12.8: empat slot antrean
- Kapasitas panggilan/pengukuran dikembalikan menjadi 4, termasuk tombol panggil berikutnya dan pemanggilan langsung. KMS tetap di luar slot.
- Sasaran bulanan dan total anak tetap memakai pemisahan v1.12.7.
- Regresi kapasitas menguji anak kelima menunggu dan dapat dipanggil setelah satu anak masuk pemeriksaan KMS.
- Validasi selesai: 53 tes JavaScript lulus; testDebugUnitTest, lintDebug, assembleDebug berhasil. APK: SIMPATIK-1.12.8-debug.apk. Belum diuji langsung di perangkat fisik.

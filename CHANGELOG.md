# Changelog

## 1.10.4 — Splash Screen dan Suara Antrean

- Menampilkan splash screen minimal 3 detik dengan logo SIMPATIK dan kredit “Dibuat oleh POLMAN Bandung”.
- Menormalkan nama anak menjadi huruf kecil sebelum dikirim ke Text to Speech agar dibaca sebagai nama, bukan dieja sebagai huruf kapital.
- Versi aplikasi menjadi `versionCode 18` / `versionName 1.10.4`.

## 1.10.3 — Login Berdasarkan Akun

- Menghapus pilihan Kader dan Administrator dari layar login.
- Menentukan peran, nama pengguna, dan cakupan RT otomatis dari klasifikasi akun di server.
- Menyederhanakan form menjadi Nama Pengguna, Kata Sandi, dan satu tombol Masuk.
- Versi aplikasi menjadi `versionCode 17` / `versionName 1.10.3`.

## 1.10.2 — Validasi Sesi Server

- Memeriksa sesi login ke endpoint terlindungi setelah koneksi server berhasil.
- Mengarahkan pengguna kembali ke login bila token lokal tidak lagi berlaku, sehingga cache bulan lama tidak dianggap sebagai data terbaru.
- Menangani respons HTTP 401 saat sinkronisasi dengan pesan masuk ulang yang jelas.
- Versi aplikasi menjadi `versionCode 16` / `versionName 1.10.2`.

## 1.10.1 — Domain Akun Ngrok Aktif

- Mengarahkan REST API Android ke static domain milik akun ngrok yang aktif.
- Memastikan domain menjawab pemeriksaan kesehatan dengan status database terhubung sebelum APK dipasang ke tablet.
- Menyamakan angka sasaran pada beranda dan halaman antrean berdasarkan data yang benar-benar tersedia di tablet.
- Versi aplikasi menjadi `versionCode 15` / `versionName 1.10.1`.

## 1.10.0 — Koneksi Domain Tetap

- Mengarahkan REST API Android ke satu static domain ngrok ber-HTTPS.
- Menghapus pemeriksaan koneksi dan alamat teknis dari halaman login.
- Memeriksa kesehatan server pada splash screen untuk pengguna yang sudah login.
- Membuka aplikasi dalam mode offline dengan pemberitahuan bila website belum menyala.
- Mencoba koneksi dan sinkronisasi ulang otomatis setelah server tersedia kembali.
- Versi aplikasi menjadi `versionCode 14` / `versionName 1.10.0`.

## 1.9.0 — Sasaran Bulanan Live

- Mengambil daftar sasaran periode aktif dari REST API Portal, bukan seluruh data induk balita.
- Menyimpan identitas sasaran dan status layanan agar QR hanya membuka anak yang masih aktif.
- Menyelesaikan status sasaran otomatis setelah hasil pengukuran tersimpan.
- Menambahkan tombol **Konfirmasi beres sesi** untuk menandai sasaran tersisa sebagai tidak hadir tanpa menghapus anak, kartu, atau riwayat.
- Mengganti cache sasaran secara aman ketika admin Portal menerbitkan periode atau daftar baru.
- Menambahkan penemuan otomatis server REST API di jaringan lokal, dengan alamat manual sebagai cadangan.
- Versi aplikasi menjadi `versionCode 13` / `versionName 1.9.0`.

## 1.8.1 — QA REST API melalui LAN

- Mengarahkan tablet fisik ke REST API Portal pada IP LAN PC, bukan alamat emulator.
- Menambahkan pengaturan alamat API dan pemeriksaan jalur API sampai PostgreSQL pada layar masuk.
- Mengizinkan HTTP lokal hanya pada APK debug untuk QA; build rilis tetap disiapkan untuk HTTPS.
- Menghapus fallback data sasaran contoh agar tablet tidak mencampur data mock dengan data live.
- Mempertahankan kontrak QR Portal `SIMPATIK:SASARAN:1:<id>:<nik>` dan kode manual `SPT-########`.
- Versi aplikasi menjadi `versionCode 12` / `versionName 1.8.1`.

## 1.8.0 — Migrasi Supabase KKN

- Memindahkan endpoint aplikasi ke proyek Supabase KKN POSYANDU TULIP.
- Mengganti kunci lama dengan publishable key yang aman untuk aplikasi klien.
- Menggunakan token sesi pengguna untuk seluruh pembacaan dan penulisan data.
- Menyatukan kontrak Android dengan skema Portal melalui view dan RPC atomik.
- Mencegah akses data sasaran tanpa login melalui Row Level Security.
- Versi aplikasi menjadi `versionCode 11` / `versionName 1.8.0`.

## 1.7.1 — Perapihan Formulir Pengukuran

- Memadatkan panel catatan antrean agar tidak mendorong formulir ke bawah.
- Menjaga kolom BB dan TB/PB tetap besar dan mudah disentuh.
- Menyusun LILA dan LIKA berdampingan agar seluruh input terlihat tanpa terpotong.
- Menyeimbangkan lebar formulir dan keypad serta merapikan footer Simpan.
- Versi aplikasi menjadi `versionCode 10` / `versionName 1.7.1`.

## 1.7.0 — Pencatatan Berbasis Antrean

- Menambahkan tab **Dari Antrean** sebagai tampilan pertama pada menu Pencatatan.
- Menambahkan tab **Semua Balita** untuk pencatatan yang tidak berasal dari antrean hari itu.
- Mengubah status anak yang dipanggil menjadi **Dalam Proses** pada Antrean dan Pencatatan.
- Mencegah anak berstatus Menunggu dibuka oleh petugas pencatatan sebelum dipanggil.
- Menyediakan catatan opsional per anak saat pendaftaran antrean melalui scan maupun pencarian manual.
- Menampilkan catatan pendaftaran pada daftar antrean, daftar Pencatatan, dan formulir hasil ukur.
- Menandai antrean **Selesai** hanya setelah hasil ukur berhasil disimpan di Android.
- Versi aplikasi menjadi `versionCode 9` / `versionName 1.7.0`.

## 1.6.2 — Pemisahan Antrean dan Pencatatan

- Menghapus tombol masuk ke formulir pengukuran dari halaman antrean.
- Menjadikan antrean khusus untuk daftar hadir, panggil, lewati, batal, dan tandai selesai.
- Memastikan hasil BB, TB/PB, LILA, dan LIKA hanya diisi melalui **Pencatatan Langsung**.
- Memperjelas keterangan dua menu pada beranda agar kader tidak mencampur alur antrean dan pencatatan.
- Versi aplikasi menjadi `versionCode 8` / `versionName 1.6.2`.

## 1.6.1 — Pencarian Antrean Ramah Pengguna

- Memisahkan **Daftar Antrean** dan **Tambah Anak ke Antrean** menjadi dua tab besar.
- Menghapus pencarian manual dari panel kanan yang sempit.
- Memperbesar kolom pencarian, nama anak, identitas wali, dan tombol tindakan.
- Menampilkan hasil dalam kartu dua kolom dengan tombol teks **Tambahkan**.
- Hasil baru muncul setelah dua karakter agar daftar tidak padat dan lebih mudah dipahami.
- Setelah anak ditambahkan, aplikasi otomatis kembali ke tab Daftar Antrean.
- Tombol kembali dari tab Tambah Anak kembali ke Daftar Antrean terlebih dahulu.

## 1.6.0 — Kartu Sasaran dan Skrining Awal

- Pemindaian QR kartu sasaran melalui Google Code Scanner pada kamera perangkat.
- Kontrak kartu bersama web: `SIMPATIK:SASARAN:1:<id>:<nik>`.
- Skrining kekurangan identitas, Buku KIA, IMD, dan status imunisasi sebelum masuk antrean.
- Pencocokan sasaran harus menghasilkan tepat satu anak; kartu yang belum diterbitkan ke perangkat ditolak dengan arahan sinkronisasi.
- Antrean tetap mencegah anak yang sama masuk dua kali pada tanggal yang sama.
- Validasi rentang BB, TB/PB, LILA, dan LIKA diselaraskan dengan pengaturan web.
- Peringatan ulang pengukuran bila BB naik lebih dari 2 kg, turun lebih dari 1,5 kg, atau TB/PB berkurang lebih dari 0,5 cm dibanding catatan sebelumnya.
- Versi aplikasi menjadi `versionCode 6` / `versionName 1.6`.

## 1.5.0 — Antrean Layanan Android

### Ditambahkan

- Antrean layanan harian pada beranda sebagai alur utama kegiatan Posyandu.
- Nomor antrean otomatis, status menunggu, dipanggil, dilayani, selesai, dan dibatalkan.
- Aksi panggil, lewati, batal, dan mulai pengukuran dengan satu layanan aktif pada satu waktu.
- Pencegahan satu anak masuk antrean lebih dari sekali pada hari yang sama.
- Penyelesaian antrean otomatis setelah pengukuran berhasil disimpan.
- Penyimpanan antrean ke SQLite Android dan pemulihan setelah aplikasi ditutup.
- Migrasi database Android versi 1 ke versi 2 tanpa menghapus data anak atau pengukuran lama.

### Disempurnakan

- Beranda memprioritaskan antrean, tetapi pencatatan langsung tetap tersedia.
- Login, antrean, dan formulir pengukuran menyesuaikan layar tablet serta ponsel portrait.
- Antrean tetap bersifat operasional lokal; pengukuran yang selesai mengikuti mekanisme sinkronisasi yang sudah ada.

## 1.4.0 — Riwayat dan Administrasi Lapangan

### Ditambahkan

- Riwayat pengukuran baca-saja dengan tab Januari–Desember dan pilihan tahun.
- Tombol koreksi dari catatan bulan yang dipilih; penyimpanan tetap menggunakan ID pengukuran yang sama.
- Pendaftaran balita baru dari beranda dengan validasi NIK dan pencegahan duplikat.
- Antrean sinkronisasi anak baru menggunakan snapshot SQLite Android dan upsert server.
- Splash screen beridentitas Program KKN Politeknik Manufaktur Bandung.
- Informasi hak cipta `© 2026 POLMAN Bandung`.

### Batas fitur

- Riwayat Android hanya menampilkan nilai BB, PB/TB, LILA, LIKA, cara ukur, revisi, dan status sinkronisasi.
- KMS, penilaian gizi, dashboard analitik, dan laporan lengkap tetap berada di Portal SIMPATIK.

## 1.3.0 — Fokus Pencatatan Lapangan

### Diubah

- Login yang berhasil sekarang langsung membuka beranda pencatatan.
- Beranda menampilkan aksi utama pencatatan, progres operasional bulan berjalan, dan antrean sinkronisasi.
- Daftar balita dapat dicari menggunakan nama atau NIK.
- Memilih balita yang sudah dicatat pada bulan berjalan membuka data yang sama dalam mode koreksi.
- Status penyimpanan membedakan data tersinkron dan data yang masih aman dalam antrean Android.
- Perhitungan tanggal menggunakan tanggal lokal perangkat agar tidak bergeser karena zona waktu UTC.

### Dikeluarkan dari alur Android

- Pendaftaran dan pengelolaan master data balita.
- Dashboard SKDN serta analitik status gizi.
- Detail kategori stunting dan rekomendasi klinis.
- KMS lengkap, cetak, dan ekspor.
- Simulasi z-score serta pengaturan rumus antropometri.
- Pengaturan Local Registry dan konfigurasi database dari layar kader.

Fitur tersebut tetap tersedia untuk dikembangkan di Portal SIMPATIK. Branch `main` tidak diubah dan tetap memuat versi 1.2 yang memiliki tampilan lengkap.

### Dipertahankan

- Login server dan sesi lokal.
- Form pengukuran ramah tablet.
- Validasi input dan dialog pemeriksaan ulang.
- SQLite offline-first, upsert, revisi, pencegahan duplikat, serta retry sinkronisasi.
- Ringkasan pengukuran sebelumnya di layar input.

## 1.2.0 — Penyimpanan Realtime dan Sinkronisasi Aman

- Menambahkan penyimpanan SQLite Android.
- Menambahkan antrean sinkronisasi dan retry otomatis.
- Menambahkan stable measurement ID dan upsert untuk mencegah double entry.
- Menambahkan validasi serta alur koreksi data.

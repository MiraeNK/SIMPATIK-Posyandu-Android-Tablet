# Changelog

## 1.12.4 — Status Koneksi dan Laporan Perangkat (21 Oktober 2026)

- Menentukan label server dari URL API yang digunakan, sehingga Cloudflare tidak lagi diberi label jaringan lokal oleh hasil penemuan otomatis atau pengaturan tersimpan.
- Menyamakan pemberitahuan hasil penemuan server dengan jalur yang ditemukan: online, lokal, atau Tailscale.
- Memperbaiki heartbeat agar menyertakan token akun dan identitas instalasi, nama/model perangkat, versi aplikasi, jenis koneksi, dan jumlah data tertunda.
- Wilayah perangkat ditentukan server dari akun yang terverifikasi; APK tidak mengirim klaim RW atau akun lain.
- Memverifikasi langsung pada Samsung SM-S711B Android 16: Cloudflare terdeteksi online, heartbeat diterima, perangkat terlihat melalui API web, dan akses tanpa sesi ditolak.
- 41 tes regresi JavaScript lulus; build APK versionCode 28 / versionName 1.12.4 berhasil.

## 1.12.3 — UI HP dan Catatan Scan Kartu (21 Oktober 2026)

- Memperbaiki halaman kosong setelah splash pada HP Samsung Android 16: pengukuran langsung menemukan `vh`, `dvh`, dan `svh` bernilai 0 walaupun tinggi WebView sekitar 782 px.
- Menggunakan batas atas/bawah kontainer tetap untuk tinggi aplikasi, sehingga halaman tidak bergantung pada satuan viewport yang keliru.
- Menggunakan tinggi relatif terhadap overlay untuk dialog konfirmasi pengukuran, agar dialog tidak ikut menyusut ke tinggi 0.
- Mempertahankan skala login HP yang ringkas dan pembaruan koneksi online dari Gemini.
- Menambahkan tombol Lihat/Sembunyikan kata sandi dengan status awal tersembunyi dan mempertahankan posisi kursor saat tombol ditekan.
- Membuat tombol Daftarkan Balita selebar satu baris pada HP, di bawah Pencatatan Langsung dan Riwayat Bulanan.
- Menyesuaikan tinggi kontainer dengan visual viewport ketika keyboard terbuka, agar tombol Masuk tetap dapat digulir ke area yang terlihat.
- Menyeragamkan ikon pilihan fitur, susunan judul/deskripsi, padding header pendaftaran, dan area sentuh navigasi minimal 48 px pada HP.
- Merapikan popup catatan setelah scan QR untuk HP/tablet: **Lewati catatan** atau **Simpan & masuk antrean**, dengan tombol batal dan penanganan Kembali Android tanpa membuat antrean.
- Membatasi popup ke tinggi layar yang terlihat ketika keyboard terbuka; isi dapat digulir, sementara kedua tindakan tetap terjangkau.
- Menambah tes regresi untuk menunda pendaftaran sampai pilihan catatan dikonfirmasi, menyimpan/melewati draf, pembatalan, serta konfirmasi berulang tanpa entri ganda.
- Versi aplikasi menjadi `versionCode 27` / `versionName 1.12.3`.

## 1.12.2 — Skala Login Khusus HP

- Memadatkan banner ilustrasi HP: gambar 80 menjadi 60 px, judul banner 21 menjadi 18 px, dan padding 24 menjadi 16 px.
- Menyesuaikan judul utama 30 menjadi 24 px dan identitas aplikasi 22 menjadi 18 px pada lebar sampai 600 px.
- Merapikan jarak form dan menggunakan kolom setinggi 48 px serta tombol Masuk minimal 50 px; teks input tetap 16 px agar mudah dibaca.
- Membatasi lebar isi login HP ke 360 px; pengaturan khusus HP tidak mengubah skala tablet lebar.
- Versi aplikasi menjadi `versionCode 26` / `versionName 1.12.2`.

## 1.12.1 — Login HP Portrait

- Menampilkan ilustrasi login sebagai banner ringkas di atas form pada layar HP; ilustrasi tidak lagi disembunyikan pada lebar di bawah 900 piksel.
- Menggunakan logo lokal SIMPATIK pada identitas login, sehingga gambar logo tetap tersedia saat offline.
- Menghapus tinggi form satu layar penuh pada HP agar form, tombol Masuk, dan banner dapat digulir bersama saat layar pendek atau keyboard terbuka.
- Mempertahankan susunan dua kolom pada tablet lebar dan memperbarui URL API Cloudflare ke tunnel aktif.
- Versi aplikasi menjadi `versionCode 25` / `versionName 1.12.1`.

## 1.12.0 — Sinkronisasi Nearby per Posyandu

- Menambahkan komunikasi tablet-ke-tablet memakai Google Nearby Connections dengan strategi Cluster.
- Memverifikasi setiap perangkat memakai identitas Posyandu dan kunci grup dari API Portal; pertukaran lintas Posyandu ditolak.
- Menyimpan konteks cluster terenkripsi dengan Android Keystore agar antrean hari ini dapat diteruskan saat jaringan server sementara putus.
- Snapshot antrean dan hasil ukur lokal yang belum terkirim diterapkan idempoten berdasarkan ID/periode serta waktu pembaruan, lalu diteruskan ke API Portal saat internet tersedia.
- Menambahkan status tablet sekitar pada beranda dan membersihkan koneksi Nearby saat pengguna keluar.
- Membutuhkan migration server `018_kunci_cluster_posyandu.sql` sebelum fitur dapat mengambil konteks cluster.

## 1.11.0 — Sinkronisasi Portal dan Antarmuka HP

- Menyelaraskan sinkronisasi check-in, status panggil, pengukuran, serta catatan dengan antrean bersama di REST API Portal.
- Menampilkan alur kerja hanya dalam dua slot. Status **Menunggu KMS di web** tetap terlihat sebagai informasi tetapi tidak menahan slot layanan.
- Menghitung semua sasaran yang sudah check-in, termasuk anak yang menunggu pemeriksaan KMS.
- Merapikan beranda, progres sasaran, dan riwayat bulanan untuk layar HP Android dengan target sentuh yang lebih mudah.
- Mengirim versi aplikasi `1.11.0` pada heartbeat server.
- Versi aplikasi menjadi `versionCode 23` / `versionName 1.11.0`.

## 1.10.8 — Login Multi-Tablet dan Tampilan HP

- Memisahkan batas kegagalan login per perangkat, dengan batas jaringan bersama yang lebih longgar untuk mencegah penguncian semua tablet oleh satu perangkat.
- Mengirim ID instalasi aplikasi saat login, menjaga spasi kata sandi apa adanya, serta membedakan pesan kredensial salah, login ditahan, dan server bermasalah.
- Merapikan layout login, antrean, pencatatan, riwayat, dan pendaftaran untuk layar HP Android.
- Versi aplikasi menjadi `versionCode 22` / `versionName 1.10.8`.

## 1.10.7 — Pendataan Susulan di Luar Sesi

- Menambahkan tab **Susulan di Luar Sesi** di menu Pencatatan setelah sesi ditutup, berisi sasaran yang ditandai tidak hadir.
- Mengarahkan kader untuk memilih anak, mengukur manual, dan menyimpan data pada periode sasaran yang benar.
- Menampilkan status **menunggu sinkron** untuk data lokal dan **Pendataan susulan tersimpan** setelah server mengonfirmasi penyimpanan.
- Menyimpan penanda pendataan susulan pada catatan sasaran di database Portal.
- Memastikan nilai LILA/LIKA bulan sebelumnya tetap terisi sebagai konteks; nilai warisan tidak dihitung sebagai pengukuran baru dan tetap mengikuti interval ukur ulang.
- Versi aplikasi menjadi `versionCode 21` / `versionName 1.10.7`.

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

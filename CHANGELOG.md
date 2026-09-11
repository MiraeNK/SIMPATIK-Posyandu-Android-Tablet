# Changelog

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

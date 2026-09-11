# Changelog

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

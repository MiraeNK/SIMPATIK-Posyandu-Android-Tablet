# Walkthrough Android SIMPATIK v1.3

## 1. Login

1. Buka aplikasi dan masukkan akun kader.
2. Aplikasi memverifikasi akun ke server.
3. Setelah berhasil, aplikasi langsung membuka halaman **Pencatatan Lapangan**.

## 2. Memulai pencatatan

1. Tekan **Mulai Pencatatan**.
2. Cari balita menggunakan nama atau NIK.
3. Perhatikan badge pada kartu balita:
   - Tidak ada badge: belum dicatat bulan ini.
   - **Menunggu sinkron**: sudah aman di Android tetapi belum terkirim.
   - **Tersimpan**: sudah terkirim ke server.
4. Tekan kartu balita untuk membuka formulir.

## 3. Mengisi pengukuran

1. Pastikan identitas balita benar.
2. Periksa tanggal pengukuran.
3. Isi BB dan PB/TB. Pilih posisi **Terlentang** atau **Berdiri**.
4. Isi LILA dan LIKA jika tersedia.
5. Tekan **Simpan** lalu periksa seluruh angka pada dialog konfirmasi.
6. Pilih **Perbaiki dulu** jika ada salah tulis, atau **Ya, simpan** jika sudah benar.

Data disimpan ke SQLite Android sebelum aplikasi mencoba mengirimkannya. Kehilangan jaringan setelah menekan simpan tidak menghilangkan data.

## 4. Memperbaiki salah tulis bulan berjalan

1. Kembali ke daftar balita.
2. Pilih kembali balita yang sama.
3. Form terbuka dalam **Mode koreksi** dengan angka sebelumnya.
4. Perbaiki angka lalu simpan.

Aplikasi menggunakan ID pengukuran yang sama dan menaikkan revisi. Tidak dibuat entri kedua untuk anak dan periode yang sama.

## 5. Sinkronisasi

1. Lihat jumlah **Antrean sinkron** di beranda.
2. Tekan **Sinkronkan sekarang** saat jaringan tersedia.
3. Jika pengiriman belum berhasil, data tetap berada di SQLite dan dicoba kembali saat perangkat online.

## 6. Membuka hasil lengkap

Gunakan website Portal SIMPATIK untuk melihat KMS lengkap, status gizi resmi, analitik, laporan, ekspor, serta koreksi data setelah kegiatan selesai.

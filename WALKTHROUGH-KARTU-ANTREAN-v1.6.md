# Walkthrough Kartu Sasaran dan Antrean v1.6

## Persiapan

1. Masuk sebagai Kader Posyandu.
2. Tekan **Sinkronkan** agar daftar sasaran terbaru dari portal tersimpan di Android.
3. Buka **Antrean Hari Ini**.

## Pendaftaran dengan kartu

1. Tekan **Pindai kartu dengan kamera** pada panel **Scan & skrining**.
2. Arahkan kamera ke QR pada kartu sasaran yang dicetak dari web.
3. Pastikan nama, wali, dan RT sesuai dengan keluarga yang hadir.
4. Periksa daftar **Skrining awal**. Item yang tampil adalah data yang masih kurang atau perlu dikonfirmasi, termasuk imunisasi.
5. Tekan **Konfirmasi masuk antrean**. Anak yang sudah berada dalam antrean hari itu tidak dapat ditambahkan kembali.

Jika kartu belum ditemukan, sinkronkan daftar sasaran lalu pindai ulang. Pencarian nama atau NIK tetap tersedia sebagai jalur manual.

## Pengukuran

1. Panggil anak, lalu tekan **Ukur**.
2. Isi BB, TB/PB, LILA, dan LIKA.
3. Bila angka di luar rentang, aplikasi meminta kader mengulang pengukuran.
4. Bila perubahan BB atau TB/PB jauh dari catatan sebelumnya, dialog tinjauan menampilkan peringatan merah. Ulangi pengukuran sebelum memilih simpan.
5. Setelah tersimpan, antrean otomatis menjadi **Selesai** dan data aman di SQLite Android sebelum dikirim ke server.

## Kode kartu

QR memakai payload berversi `SIMPATIK:SASARAN:1:<id>:<nik>`. Tulisan pendek `SPT-00000110` dapat digunakan untuk pencarian manual pada web. Versi payload mencegah aplikasi membaca format kartu yang tidak dikenali secara diam-diam.

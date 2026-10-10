# Walkthrough Kartu Sasaran dan Antrean v1.6

## Persiapan

1. Masuk sebagai Kader Posyandu.
2. Tekan **Sinkronkan** agar daftar sasaran terbaru dari portal tersimpan di Android.
3. Buka **Antrean Hari Ini**, lalu pilih tab **Tambah Anak ke Antrean**.

## Pendaftaran dengan kartu

1. Tekan **Pindai Kartu dengan Kamera** pada tab **Tambah Anak ke Antrean**.
2. Arahkan kamera ke QR pada kartu sasaran yang dicetak dari web.
3. Pastikan nama, wali, dan RT sesuai dengan keluarga yang hadir.
4. Periksa daftar **Skrining awal**. Item yang tampil adalah data yang masih kurang atau perlu dikonfirmasi, termasuk imunisasi.
5. Isi **Catatan untuk petugas pencatatan** bila ada informasi yang perlu diperiksa saat pengukuran.
6. Tekan **Konfirmasi masuk antrean**. Anak yang sudah berada dalam antrean hari itu tidak dapat ditambahkan kembali.

Jika kartu belum ditemukan, sinkronkan daftar sasaran lalu pindai ulang. Untuk pencarian manual, ketik sedikitnya dua huruf nama atau angka NIK. Hasil tampil sebagai kartu besar; tekan **Tambahkan** pada anak yang sesuai.

## Menyelesaikan antrean

1. Tekan **Panggil** saat giliran anak tiba.
2. Status anak berubah menjadi **Dalam Proses**.
3. Halaman antrean tidak membuka formulir pengukuran dan tidak menyelesaikan antrean secara manual.

## Pencatatan hasil ukur

1. Kembali ke beranda dan pilih **Pencatatan Langsung**.
2. Tab **Dari Antrean** terbuka pertama. Anak yang dipanggil memiliki status **Dalam Proses**.
3. Pilih anak tersebut. Catatan pendaftaran tampil di atas formulir.
4. Gunakan tab **Semua Balita** bila pencatatan tidak berasal dari antrean.
5. Isi BB, TB/PB, LILA, dan LIKA.
6. Bila angka di luar rentang, aplikasi meminta kader mengulang pengukuran.
7. Bila perubahan BB atau TB/PB jauh dari catatan sebelumnya, dialog tinjauan menampilkan peringatan merah. Ulangi pengukuran sebelum memilih simpan.
8. Setelah tersimpan, data aman di SQLite Android dan antrean otomatis berubah menjadi **Selesai**.

## Kode kartu

QR memakai payload berversi `SIMPATIK:SASARAN:1:<id>:<nik>`. Tulisan pendek `SPT-00000110` dapat digunakan untuk pencarian manual pada web. Versi payload mencegah aplikasi membaca format kartu yang tidak dikenali secara diam-diam.

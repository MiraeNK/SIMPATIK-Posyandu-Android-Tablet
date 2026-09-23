# Walkthrough Antrean Android SIMPATIK v1.5

## Membuka antrean

1. Login sebagai kader atau administrator.
2. Dari beranda, tekan **Antrean Hari Ini**.
3. Ringkasan bagian atas menampilkan jumlah **Menunggu**, **Dilayani**, dan **Selesai**.

Antrean disimpan di SQLite Android. Urutan tetap tersedia setelah aplikasi ditutup atau perangkat sedang offline.

## Menambahkan anak yang hadir

1. Gunakan kolom **Tambah antrean** di sisi kanan pada tablet atau bagian bawah pada ponsel.
2. Cari anak menggunakan nama atau NIK.
3. Tekan anak yang datang ke Posyandu.
4. Aplikasi memberikan nomor `A01`, `A02`, dan seterusnya.

Satu anak hanya dapat memiliki satu antrean aktif atau selesai pada hari yang sama. Anak yang sudah masuk ditandai dan tidak dapat ditambahkan kembali.

## Memanggil dan mengatur urutan

- Tekan **Panggil** untuk menampilkan anak pada panel layanan utama.
- Hanya satu anak yang dapat dipanggil atau dilayani pada satu waktu. Memanggil anak lain mengembalikan anak sebelumnya ke status menunggu.
- Tekan **Lewati** jika anak belum siap. Nomor antrean tetap sama, tetapi anak dipindahkan ke bagian akhir urutan menunggu.
- Tekan **Batal** bila anak pulang atau tidak jadi dilayani. Catatan diubah menjadi dibatalkan dan tidak dihitung sebagai antrean aktif.

## Mengukur dan menyelesaikan layanan

1. Pada anak yang dipanggil, tekan **Mulai Pengukuran** atau **Ukur**.
2. Periksa identitas dan tanggal pengukuran.
3. Isi BB, PB/TB, LILA, dan LIKA menggunakan keypad.
4. Tekan **Simpan**, periksa kembali angka, kemudian konfirmasi.
5. Setelah data berhasil masuk ke penyimpanan Android, antrean anak otomatis berubah menjadi **Selesai**.

Jika penyimpanan gagal, antrean tidak diselesaikan agar anak tidak hilang dari alur layanan.

## Pencatatan tanpa antrean

Gunakan **Pencatatan Langsung** pada beranda untuk koreksi atau kondisi khusus yang tidak melalui antrean hari itu. Riwayat, pendaftaran balita, dan sinkronisasi tetap tersedia seperti versi sebelumnya.

## Saat jaringan terputus

Antrean, daftar anak, dan pengukuran tetap tersimpan di Android. Pengukuran yang belum terkirim muncul pada indikator antrean sinkronisasi dan dapat dikirim kembali dengan tombol **Sinkronkan**.

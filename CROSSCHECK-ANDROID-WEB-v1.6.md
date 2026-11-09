# Cross-check Android dan Portal Web v1.7

## Pembagian fitur

| Area | Android | Web |
| --- | --- | --- |
| Master sasaran dan penerbitan kartu | Membaca salinan sasaran | Mengelola sasaran dan mencetak QR |
| Check-in dan antrean | Scan kartu, antrean lapangan, tahan offline dan sinkron lintas perangkat | Check-in kamera laptop serta pemantauan antrean bersama |
| Skrining identitas | Menampilkan kekurangan saat scan | Menentukan kelengkapan dari master sasaran |
| Pengukuran dan koreksi | Input utama, validasi, penyimpanan SQLite | Menampilkan hasil yang sudah terkirim |
| KMS, z-score, edukasi, rujukan | Ringkasan operasional saja | Analitik lengkap dan aturan terpusat |
| WhatsApp dan laporan F1 | Tidak menjadi menu utama | Tanggung jawab portal/backend |

## Kontrak sinkronisasi antrean

- Android dan Portal memakai REST API yang sama: `GET /antrean?periodeId=<id>&tanggal=YYYY-MM-DD` untuk mengambil snapshot bersama.
- Android membuat check-in dengan `POST /antrean` berisi `periodeId`, `sasaranId`, `tanggal`, dan `catatan` (boleh kosong). Server mengembalikan item kanonis, termasuk ID antrean, nomor, urutan, status, dan waktu check-in.
- Perubahan status dan catatan dikirim dengan `PATCH /antrean/:id`. Status kerja `waiting`, `called`, dan `serving` masuk daftar aktif; `kms_review` ditampilkan sebagai status tindak lanjut di Portal dan tidak mengambil slot kerja; `done` ditetapkan setelah konfirmasi KMS di web.
- Kunci unik periode/tanggal/sasaran di server mencegah check-in ganda lintas tablet. Android menarik snapshot server dahulu, menggabungkan dengan perubahan lokal yang belum terkirim, lalu mengirim perubahan tertunda.
- SQLite Android dan cache lokal mempertahankan antrean saat offline. Layar menunjukkan bila antrean bersama gagal dimuat atau belum tersinkron; perubahan lokal dicoba kembali saat koneksi tersedia.

## Pembagian alur layanan

- Android menyediakan dua slot panggil/ukur. Setelah hasil ukur tersimpan, anak keluar dari slot dan tetap terlihat pada daftar **Menunggu KMS di web**.
- Portal menerima hasil ukur, menampilkan KMS/analitik, dan mengonfirmasi penyelesaian pemeriksaan KMS. Status `done` tidak boleh disimpulkan hanya dari Android.
- Penerbitan kartu, master sasaran, dan daftar antrean web memakai ID sasaran server yang sama dengan aplikasi.

Kontrak ini diperiksa pada kode klien Android dan API server lokal. Perbarui dokumen dan regresi integrasi bersama-sama bila route atau payload server berubah.

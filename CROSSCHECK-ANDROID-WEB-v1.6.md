# Cross-check Android dan Portal Web v1.6

## Pembagian fitur

| Area | Android | Web |
| --- | --- | --- |
| Master sasaran dan penerbitan kartu | Membaca salinan sasaran | Mengelola sasaran dan mencetak QR |
| Check-in dan antrean | Operasional lapangan, tahan offline | Check-in kamera laptop untuk meja pendaftaran |
| Skrining identitas | Menampilkan kekurangan saat scan | Menentukan kelengkapan dari master sasaran |
| Pengukuran dan koreksi | Input utama, validasi, penyimpanan SQLite | Menampilkan hasil yang sudah terkirim |
| KMS, z-score, edukasi, rujukan | Ringkasan operasional saja | Analitik lengkap dan aturan terpusat |
| WhatsApp dan laporan F1 | Tidak menjadi menu utama | Tanggung jawab portal/backend |

## Kontrak yang sudah sama

- QR: `SIMPATIK:SASARAN:1:<id>:<nik>`.
- Kode cetak: `SPT-<id 8 digit>`.
- Scan hanya diterima bila cocok tepat dengan satu sasaran.
- Antrean harian memakai identitas anak dan mencegah entri ganda.
- Batas ukur web dan Android: BB 1–40 kg, TB/PB 30–130 cm, LILA 8–25 cm, LIKA 30–60 cm.

## Batas integrasi saat ini

Branch web `design` masih berupa demo frontend. Penerbitan kartu, pemindaian kamera laptop, skrining, dan daftar antrean sudah berfungsi di browser, tetapi antrean web masih berada di `localStorage`. Android menyimpan antrean di SQLite dan mengambil sasaran dari Supabase. Belum ada endpoint server yang menyatukan antrean dari dua perangkat.

Backend berikutnya perlu menyediakan tabel antrean dengan kunci unik `(tanggal_layanan, sasaran_id)`, endpoint lookup kartu, publikasi sasaran per wilayah, dan idempotency key untuk check-in. Setelah itu web dan Android harus memakai baris antrean yang sama agar status panggil/layani tidak tumpang tindih.

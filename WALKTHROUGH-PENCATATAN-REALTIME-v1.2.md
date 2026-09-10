# Walkthrough Pencatatan Realtime SIMPATIK Posyandu v1.2

## Tujuan perubahan

Pencatatan pengukuran sekarang memakai pola **simpan lokal lebih dahulu, sinkronkan setelahnya**. Saat kader menekan simpan, data langsung masuk ke SQLite di penyimpanan internal Android. Koneksi internet tidak menentukan keberhasilan penyimpanan di tablet.

Setiap balita hanya mempunyai satu catatan aktif dalam satu periode bulanan. Koreksi nilai atau tanggal memperbarui catatan tersebut dengan ID yang sama. Tombol ditekan dua kali, aplikasi ditutup saat offline, atau pengiriman diulang tidak membentuk entri pengukuran baru.

## Alur penggunaan

1. Masuk ke **Penimbangan Balita**, lalu pilih **Mulai Pencatatan**.
2. Pilih balita. Jika periode tanggal ukur tersebut sudah memiliki data, formulir otomatis terisi dan menampilkan label **Mode koreksi**.
3. Pastikan **Tanggal ukur** benar. Tanggal boleh dikoreksi. Jika koreksi memindahkan bulan, catatan lama ikut dipindahkan dan tetap memakai ID yang sama.
4. Isi berat badan, panjang/tinggi badan, LILA, dan LIKA. Berat serta panjang/tinggi wajib diisi; LILA dan LIKA boleh dikosongkan.
5. Tekan **Simpan** atau **Tinjau Koreksi**. Aplikasi memeriksa data sebelum membuka konfirmasi.
6. Periksa ringkasan, lalu tekan **Ya, simpan**. SQLite Android menyimpan data secara langsung dan atomik.
7. Aplikasi kembali ke daftar atau Kartu Kontrol. Sinkronisasi Supabase berjalan di latar belakang.

## Memperbaiki salah tulis

Ada dua jalur koreksi:

- Buka **Mulai Pencatatan** dan pilih balita yang sama. Data periode aktif otomatis dimuat ke formulir.
- Buka **Kartu Kontrol**, pilih tahun dan bulan, kemudian tekan **Koreksi Data**.

Setelah koreksi disimpan, nomor revisi lokal bertambah. Baris SQLite dan baris Supabase menggunakan `id_pengukuran` yang sama, sehingga nilai lama diperbarui tanpa membuat riwayat ganda.

## Validasi yang diterapkan

- NIK pendaftaran wajib tepat 16 digit dan tidak boleh sudah terdaftar.
- Tanggal ukur wajib valid, tidak boleh di masa depan, dan tidak boleh sebelum tanggal lahir.
- Berat badan wajib berada pada rentang 1–40 kg.
- Panjang/tinggi badan wajib berada pada rentang 30–130 cm.
- LILA, jika diisi, harus berada pada rentang 5–40 cm.
- LIKA, jika diisi, harus berada pada rentang 20–65 cm.
- Tombol simpan dikunci selama transaksi berjalan untuk mencegah klik ganda.
- Database Android memiliki batas unik gabungan NIK dan periode.

Rentang tersebut berfungsi sebagai pelindung salah ketik, bukan penetapan diagnosis. Nilai di luar rentang harus diperiksa kembali sebelum dicatat.

## Penyimpanan dan sinkronisasi

SQLite Android menyimpan dua kelompok data:

- snapshot daftar balita untuk pemulihan data saat aplikasi dibuka kembali;
- pengukuran beserta status `pending` atau `synced`, nomor revisi, waktu dibuat, dan waktu diperbarui.

Jika Supabase tersedia, aplikasi memakai upsert dengan konflik pada `id_pengukuran`. Jika unggahan gagal, status tetap `pending`; data tidak dihapus. Aplikasi mencoba lagi saat dibuka dan ketika Android memberi sinyal bahwa koneksi internet kembali. Sebelum mengunduh data cloud, aplikasi mengirim antrean lokal terlebih dahulu agar koreksi lokal tidak tertimpa data server yang lebih lama.

Pada Kartu Kontrol, badge menunjukkan kondisi catatan:

- **Aman di Android** berarti sudah tbisaersimpan di tablet dan masih menunggu sinkronisasi;
- **Tersinkron Cloud** berarti server telah menerima catatan.

## Perlindungan data ganda dan tumpang tindih

ID baru dibentuk secara tetap dari identitas balita dan periode, misalnya `ukur_3273..._202609`. Database Android juga menerapkan `UNIQUE(nik, id_periode)`. Di Supabase, permintaan dikirim sebagai upsert berdasarkan `id_pengukuran`. Tiga lapisan ini membuat pengiriman ulang memperbarui data yang sama.

Riwayat lama yang berasal dari Supabase mempertahankan ID aslinya. Saat catatan itu dikoreksi, aplikasi memakai kembali ID tersebut. Jika tanggal koreksi berpindah bulan, slot bulan lama di cache UI dihapus setelah transaksi Android berhasil.

## Hasil verifikasi

- 11 tes regresi JavaScript lulus: login/sesi, navigasi Back, validasi pengukuran, koreksi pada periode sama, pemindahan tanggal antarbulan, upsert cloud, antrean saat server gagal, dan NIK duplikat.
- Unit test Android lulus.
- Kompilasi Kotlin dan `assembleDebug` berhasil dengan Gradle 9.6.
- Isi `assets/index.html` di APK diverifikasi sama dengan sumber aplikasi.
- Lint mencapai tahap `lintAnalyzeDebug`, tetapi tidak menghasilkan keluaran lanjutan dan dihentikan setelah proses berhenti membuat kemajuan. Build, kompilasi Kotlin, dan tes tetap selesai sukses.
- Pengujian visual/perangkat belum dijalankan karena tidak ada emulator atau perangkat yang terhubung pada sesi build ini.

## APK

Nama hasil: `build_apk/SIMPATIK_Posyandu_Tablet_v1.2_realtime.apk`

Versi aplikasi: `1.2` (`versionCode 2`).

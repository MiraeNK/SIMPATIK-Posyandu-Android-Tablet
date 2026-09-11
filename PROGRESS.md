# Progress Pengembangan

Terakhir diperbarui: 11 September 2026

## Selesai

- [x] Membuat branch `codex/fokus-input-tablet` dari versi terbaru `main` (`7ffbc19`).
- [x] Menjaga branch `main` tanpa perubahan.
- [x] Menetapkan batas tanggung jawab Android dan Portal.
- [x] Merampingkan navigasi Android menjadi login → beranda → daftar balita → formulir.
- [x] Menghapus akses pengguna ke KMS, analitik gizi, rumus, laporan, master data, dan pengaturan database.
- [x] Menghapus kalkulasi status gizi contoh dari alur aplikasi.
- [x] Menambahkan progres pencatatan bulan berjalan.
- [x] Menambahkan tombol sinkronisasi manual dan indikator antrean.
- [x] Mempertahankan SQLite, validasi, koreksi periode aktif, upsert, dan retry offline.
- [x] Memperbaiki tanggal lokal perangkat.
- [x] Memperbarui README, changelog, dan walkthrough versi 1.3.

## Verifikasi

- [x] Regression test Node.js: 11/11 lulus.
- [x] Gradle unit test: lulus dalam build 42 task.
- [x] Build APK debug versi 1.3: berhasil.
- [x] Pemeriksaan metadata APK: versionCode 3, versionName 1.3, min SDK 24, target SDK 37.
- [ ] Uji pada emulator atau tablet fisik.

APK hasil build: `build_apk/SIMPATIK_Posyandu_Tablet_v1.3_fokus_input.apk`

SHA-256: `E7F325B1143DB0CAF29792F8D95829EC5867BB987F19860EB7C06ABF1DB24D26`

Uji perangkat belum dapat dilakukan karena tidak ada emulator atau perangkat Android yang terhubung saat build.

## Integrasi Portal yang masih diperlukan

- [ ] Tetapkan endpoint sinkronisasi resmi Portal sebagai pengganti kontrak Supabase sementara.
- [ ] Selaraskan identitas anak menggunakan `anak_id` server; NIK tetap sebagai data pencarian, bukan primary key.
- [ ] Selaraskan nama field dan enum pengukuran dengan model Portal.
- [ ] Tambahkan autentikasi perangkat dan idempotency key di endpoint Portal.
- [ ] Portal menghitung status gizi setelah menerima pengukuran dan menyimpan audit koreksi.
- [ ] Tetapkan aturan penutupan periode: tablet mengoreksi periode aktif, Portal mengoreksi periode yang sudah ditutup.

Dokumen ini harus diperbarui setiap kali satu tahap verifikasi atau integrasi selesai.

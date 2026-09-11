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
- [x] Mengembalikan riwayat baca-saja dengan tab bulan dan pilihan tahun.
- [x] Mengaktifkan koreksi nilai dari riwayat Android.
- [x] Mengembalikan pendaftaran balita baru pada beranda.
- [x] Menyimpan pendaftaran ke Android sebelum upsert server.
- [x] Menambahkan identitas POLMAN Bandung dan hak cipta 2026 pada splash screen.

## Verifikasi

- [x] Regression test Node.js versi 1.4: 14/14 lulus.
- [x] Gradle unit test versi 1.4: `testDebugUnitTest` lulus.
- [x] Build APK debug versi 1.4: `assembleDebug` lulus.
- [x] Pemeriksaan metadata APK versi 1.4: versionCode 4, versionName 1.4, minSdk 24, targetSdk 37.
- [x] Uji pada emulator Pixel Tablet API 33: splash, beranda, riwayat bulanan, dan pendaftaran tampil dengan benar.

APK: `build_apk/SIMPATIK_Posyandu_Tablet_v1.4_operasional.apk`

SHA-256: `6A2F06AF5C8E63F317D0B521FC1851A5A4E7B885B37EB57924F66A240E8798ED`

Ukuran: 12.838.983 byte. Aset `index.html` di dalam APK sama dengan sumber saat build.

## Integrasi Portal yang masih diperlukan

- [ ] Tetapkan endpoint sinkronisasi resmi Portal sebagai pengganti kontrak Supabase sementara.
- [ ] Selaraskan identitas anak menggunakan `anak_id` server; NIK tetap sebagai data pencarian, bukan primary key.
- [ ] Selaraskan nama field dan enum pengukuran dengan model Portal.
- [ ] Tambahkan autentikasi perangkat dan idempotency key di endpoint Portal.
- [ ] Portal menghitung status gizi setelah menerima pengukuran dan menyimpan audit koreksi.
- [ ] Tetapkan aturan penutupan periode: tablet mengoreksi periode aktif, Portal mengoreksi periode yang sudah ditutup.

Dokumen ini harus diperbarui setiap kali satu tahap verifikasi atau integrasi selesai.

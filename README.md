# SIMPATIK Posyandu - Tablet Android App 📱👶
> **Sistem Informasi Manajemen Posyandu Terpadu & Intervensi Klinis**  
> *Pengembangan Aplikasi Tablet Android untuk Kader Posyandu — Program KKN POLMAN 2026*

[![Android](https://img.shields.io/badge/Platform-Android%2013%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Vue.js](https://img.shields.io/badge/Frontend-Vue.js%203-brightgreen.svg)](https://vuejs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Styling-Tailwind%20CSS-38bdf8.svg)](https://tailwindcss.com/)
[![Standard](https://img.shields.io/badge/Antropometri-WHO%20%2F%20Kemenkes%20RI%202020-orange.svg)](https://kemkes.go.id)

---

## 📌 Ringkasan Proyek

**SIMPATIK Posyandu** adalah aplikasi tablet berbasis Android yang dirancang khusus untuk memodernisasi dan mengotomatisasi pencatatan posyandu di lapangan. Aplikasi ini mengutamakan prinsip **offline-first**, kecepatan entri data, serta keakuratan analisis medis stunting menggunakan standar baku antropometri internasional.

Aplikasi ini menggabungkan performa native **Android Jetpack Compose & WebView** dengan kelincahan antarmuka reaktif **Vue.js 3**, memungkinkan kader posyandu melakukan pencatatan penimbangan secara instan tanpa terhambat kendala koneksi internet di balai RW.

---

## ✨ Fitur Utama

### 1. 📊 Dashboard Pengukuran & Pemantauan Harian
- Ringkasan sasaran balita (Total Sasaran: **131 Balita**, status Sudah Diukur vs Belum Diukur).
- Pemantauan status gizi makro berdasarkan Tinggi Badan per Umur (TB/U):
  - **Normal / Tinggi**
  - **Risiko Pendek (*Borderline*)**
  - **Pendek (*Stunted*)**
  - **Sangat Pendek (*Severely Stunted*)**
- Tombol aksi cepat: *Mulai Pencatatan*, *Kartu Kontrol*, dan *Pengaturan*.

### 2. 📋 Layar Mandiri Detailing Gizi Balita (Dedicated Screen)
- Layar penuh (*tab dedicated*) tanpa modal pop-up yang sempit.
- Filter *switcher* instan antar kategori status gizi.
- **Panduan & SOP Klinis Kemenkes RI**: Interpretasi klinis, ambang batas Z-score, dan rencana rujukan/tindakan kader.
- **Tabel Ambang Batas Resmi**: Nilai batas baku untuk indikator TB/U, BB/U, BB/TB, dan LiLA.
- **Tabel Data Anak Detil**: Daftar lengkap balita pada kategori terpilih beserta usia, jenis kelamin, orang tua, RT, dan tombol pintas ke Kartu Kontrol.

### 3. 📑 Kartu Kontrol & KMS Digital
- Timeline bulanan interaktif (Januari – Desember, multi-tahun 2025 & 2026).
- Kartu metrik pertumbuhan:
  - **Berat Badan (kg)**
  - **Panjang / Tinggi Badan (cm)**
  - **Lingkar Lengan Atas / LiLA (cm)**
  - **Lingkar Kepala / LiKA (cm)**
- Penanganan data belum diukur secara ringkas dan rapi dengan label **`N/A`** (misal `N/A cm`).
- Evaluasi Z-score medis otomatis untuk 3 indeks pertumbuhan (BB/U, TB/U, BB/TB).

### 4. ⚙️ Menu Pengaturan & Simulasi Antropometri
- **Tab Rumus & Standar Antropometri**:
  - Penjelasan matematis metode **Cole's LMS** WHO:
    $$\\Z = \\frac{(y / M)^L - 1}{L \\cdot S}\\$$
  - Pilihan standar baku (Permenkes No. 2/2020, WHO Child Growth Standards 2006, WHO 2007, CDC 2000).
  - Koreksi posisi pengukuran panjang/tinggi badan (±0.7 cm).
  - **Kalkulator Z-Score Interaktif** untuk simulasi perhitungan di tempat.
- **Tab Profil Posyandu & Kader**: Identitas Posyandu Melati, RW/Desa/Kecamatan, dan data kader bertugas yang tersimpan persisten.
- **Tab Database & Sinkronisasi Server**:
  - Dukungan mode **Local JSON (Offline-First)** untuk operasional lapangan.
  - Skema integrasi **Hybrid Sync ke Docker PostgreSQL** untuk rekonsiliasi data berkala ke Puskesmas.
  - Fitur Backup/Restore file `.json` lokal dan template DDL SQL PostgreSQL.

---

## 🗂️ Struktur Data & Manajemen Basis Data

Aplikasi ini menggunakan basis data riil dari rekap posyandu yang telah melalui proses validasi dan pembersihan:
- **131 Balita Riil**: Seluruh balita aktif usia 0–59 bulan (kelahiran 2021–2026) dengan 1.507 catatan riwayat penimbangan terverifikasi.
- **Pemisahan Segmen Lansia**: 19 data individu usia sekolah dan lansia dipisahkan ke `segment_lansia_terpisah.json` untuk modul masa depan.
- **Penyimpanan Lokal Persisten**: Setiap pengukuran langsung disimpan secara atomik ke SQLite internal Android. `localStorage` dipakai sebagai cache antarmuka, sedangkan antrean SQLite menjaga data offline hingga berhasil disinkronkan ke server.

```
outputdatabasesementara_json/
├── balita_clean_for_app.json      # Master data 131 balita siap pakai
├── anak.json                      # Tabel identitas anak
├── pengukuran.json                # Tabel pengukuran berkala
├── status_gizi.json               # Hasil evaluasi Z-score
└── segment_lansia_terpisah.json   # Data terpisah segmen lansia / usia lanjut
```

---

## 🛠️ Arsitektur Teknologi

```
┌─────────────────────────────────────────────────────────┐
│                 SIMPATIK Tablet Posyandu                │
├─────────────────────────────────────────────────────────┤
│  Antarmuka Pengguna (UI/UX)                             │
│  - Vue.js 3 (Composition / Reactive State)              │
│  - Tailwind CSS (Responsive Design untuk Tablet 1280dp) │
│  - Standar Baku Antropometri (WHO Cole's LMS Table)     │
├─────────────────────────────────────────────────────────┤
│  Jembatan Native (Android JavascriptBridge)             │
│  - window.AndroidBridge.simpanDataPengukuran(payload)   │
├─────────────────────────────────────────────────────────┤
│  Lapisan Native Android                                 │
│  - Kotlin + Jetpack Compose (Edge-to-Edge Container)    │
│  - Android WebView (DOM Storage, File & Hardware Accel) │
│  - SQLite internal, antrean offline, upsert Supabase    │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Panduan Membangun & Menjalankan (Build & Run)

### Kebutuhan Sistem
- **Android Studio**: Versi Iguana / Koala / Ladybug atau yang lebih baru.
- **JDK**: Java Development Kit versi 17.
- **Android SDK**: Target SDK 34 (Android 14) / Minimum SDK 24 (Android 7.0).
- **Perangkat**: Tablet Android fisik atau Android Virtual Device (AVD) — disarankan resolusi tablet (misalnya Pixel Tablet 1280x800).

### Langkah Menjalankan
1. **Clone repositori**:
   ```bash
   git clone https://github.com/MiraeNK/SIMPATIK-Posyandu-Android-Tablet.git
   cd SIMPATIK-Posyandu-Android-Tablet
   ```

2. **Kompilasi APK Debug**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Pasang langsung ke Perangkat / Emulator Tablet**:
   ```bash
   ./gradlew installDebug
   ```

4. **Jalankan Aplikasi**:
   Aplikasi akan otomatis terpasang dengan nama **SIMPATIK Posyandu** (`com.example.simpatikposyandu`).

---

## 📂 Struktur Direktori Proyek

```
SIMPATIK-Posyandu-Android-Tablet/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   ├── index.html           # SPA Tablet Posyandu (Vue.js 3 + Tailwind)
│   │       │   ├── data_balita.js       # Seed dataset 131 balita riil Posyandu
│   │       │   └── who-lms.json         # Tabel referensi WHO Cole's LMS
│   │       ├── java/com/example/simpatikposyandu/
│   │       │   ├── MainActivity.kt      # Native container, WebView & AndroidAppBridge
│   │       │   └── ui/theme/            # Jetpack Compose Theme & Colors
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── outputdatabasesementara_json/        # Rekap JSON data posyandu riil & lansia
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── .gitignore
```

---

## 👥 Kontributor & Pengembang

Dikembangkan sebagai bagian dari program pengabdian masyarakat dan inovasi digital kesehatan:
- **Program**: KKN POLMAN 2026
- **Lokasi Fokus**: Posyandu Melati
- **Pengembang**: [@MiraeNK](https://github.com/MiraeNK)

---

## 📄 Lisensi
Hak Cipta © 2026 KKN POLMAN. Didistribusikan untuk kepentingan pelayanan posyandu dan pencegahan stunting nasional.

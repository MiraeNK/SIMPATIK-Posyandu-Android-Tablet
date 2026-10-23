# Pemulihan unggahan KMS dan daftar anak

Pasang APK 1.12.6 sebagai pembaruan pada setiap perangkat, tanpa menghapus data aplikasi. Buka aplikasi dengan akun Posyandu/RW yang sama, aktifkan Bluetooth dan izin Perangkat sekitar (serta lokasi bila diminta Android). Aplikasi perlu tetap terbuka untuk menjalankan sinkronisasi berkala.

Hasil ukur disimpan di SQLite dahulu. Bila unggahan gagal, slot pengukuran tetap dibebaskan dan daftar informasi KMS menyebut menunggu unggahan. Tiap 15 detik aplikasi mengirim snapshot ke peer dan memeriksa server; bila server dapat dijangkau, data tertunda dicoba kirim lagi. Peer yang menerima data dan memiliki akses unggah bisa mengirimkannya. Konfirmasi hanya membersihkan payload yang sama agar koreksi baru tidak terhapus.

Daftar terbaru ditarik dari server setiap siklus sehat. Master anak baru tidak harus menunggu masuk sasaran agar muncul dalam daftar aplikasi; memasukkannya menjadi sasaran layanan bulanan tetap mengikuti administrasi web. Pengukuran pending tidak dihapus oleh unduhan.

Data yang tertahan pada perangkat lama tetap berada di perangkat tersebut. Perangkat itu perlu dipasang pembaruan dan dibuka agar data dapat dikirim atau diteruskan. Jangan uninstall/hapus penyimpanan sebelum semua kiriman dikonfirmasi.

Verifikasi: 47 regresi JS, 14 tes HTTP/database pada skema terisolasi. Belum ada pengujian tiga tablet fisik karena ADB tidak mendeteksi perangkat.

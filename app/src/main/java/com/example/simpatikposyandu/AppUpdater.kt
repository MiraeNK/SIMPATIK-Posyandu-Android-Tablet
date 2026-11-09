package com.example.simpatikposyandu

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Pembaruan APK dari server SIMPATIK (bukan Google Play).
 *
 * Alur: unduh ke cache aplikasi -> cocokkan SHA-256 dari server -> pastikan
 * paket sama dan versi lebih baru -> buka layar pemasang Android. Android
 * selalu meminta satu konfirmasi pemasangan; izin "instal dari sumber ini"
 * diminta sekali dan pemasangan dilanjutkan otomatis setelah kembali.
 */
class AppUpdater(
    private val activity: Activity,
    private val kirimKeWeb: (String) -> Unit,
) {
    @Volatile private var sedangMengunduh = false
    @Volatile private var dibatalkan = false
    private var menungguIzin: File? = null

    private fun berkasTujuan(): File = File(File(activity.cacheDir, "pembaruan").apply { mkdirs() }, "simpatik-update.apk")

    fun infoVersi(): String {
        val info = paketTerpasang()
        @Suppress("DEPRECATION")
        val kode = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        return JSONObject()
            .put("versionCode", kode)
            .put("versionName", info.versionName ?: "")
            .put("bolehPasang", bolehMemasang())
            .toString()
    }

    private fun paketTerpasang(): PackageInfo =
        if (Build.VERSION.SDK_INT >= 33) activity.packageManager.getPackageInfo(activity.packageName, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") activity.packageManager.getPackageInfo(activity.packageName, 0)

    private fun bolehMemasang(): Boolean =
        Build.VERSION.SDK_INT < 26 || activity.packageManager.canRequestPackageInstalls()

    private fun status(tahap: String, extra: JSONObject.() -> Unit = {}) {
        val data = JSONObject().put("tahap", tahap).apply(extra).toString()
        kirimKeWeb("window.handleStatusPembaruan && window.handleStatusPembaruan(${JSONObject.quote(data)})")
    }

    fun mulai(url: String, sha256: String, versionCode: Long): String {
        val alamat = runCatching { URL(url) }.getOrNull()
        if (alamat == null || alamat.protocol !in setOf("https", "http") || !sha256.matches(Regex("^[a-f0-9]{64}$"))) {
            return JSONObject().put("ok", false).put("error", "Alamat atau checksum pembaruan tidak valid").toString()
        }
        if (sedangMengunduh) return JSONObject().put("ok", true).put("berjalan", true).toString()
        sedangMengunduh = true
        dibatalkan = false
        Thread {
            try {
                val berkas = unduh(alamat)
                if (dibatalkan) { berkas.delete(); status("batal"); return@Thread }
                status("memeriksa")
                val hash = sha256(berkas)
                require(hash == sha256) { "Checksum berkas tidak cocok. Unduh ulang saat jaringan stabil." }
                periksaPaket(berkas, versionCode)
                activity.runOnUiThread { pasang(berkas) }
            } catch (error: Exception) {
                Log.e("AppUpdater", "Pembaruan gagal", error)
                berkasTujuan().delete()
                status("gagal") { put("pesan", error.message ?: "Pembaruan gagal") }
            } finally {
                sedangMengunduh = false
            }
        }.start()
        return JSONObject().put("ok", true).toString()
    }

    fun batal() { dibatalkan = true }

    private fun unduh(alamat: URL): File {
        val tujuan = berkasTujuan()
        val sementara = File(tujuan.parentFile, tujuan.name + ".part")
        val koneksi = (alamat.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            instanceFollowRedirects = true
        }
        try {
            if (koneksi.responseCode != 200) error("Server menolak unduhan (HTTP ${koneksi.responseCode})")
            val total = koneksi.contentLengthLong
            var diterima = 0L
            var persenTerakhir = -1
            status("mengunduh") { put("persen", 0); put("total", total) }
            koneksi.inputStream.use { masuk ->
                sementara.outputStream().use { keluar ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        if (dibatalkan) break
                        val n = masuk.read(buffer)
                        if (n < 0) break
                        keluar.write(buffer, 0, n)
                        diterima += n
                        val persen = if (total > 0) ((diterima * 100) / total).toInt() else -1
                        if (persen != persenTerakhir) {
                            persenTerakhir = persen
                            status("mengunduh") { put("persen", persen); put("diterima", diterima); put("total", total) }
                        }
                    }
                }
            }
        } finally {
            koneksi.disconnect()
        }
        if (dibatalkan) { sementara.delete(); return tujuan }
        if (tujuan.exists()) tujuan.delete()
        check(sementara.renameTo(tujuan)) { "Berkas pembaruan tidak dapat disimpan" }
        return tujuan
    }

    private fun sha256(berkas: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        berkas.inputStream().use { masuk ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = masuk.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /** Menolak APK paket lain atau versi yang tidak lebih baru. */
    private fun periksaPaket(berkas: File, versionCodeServer: Long) {
        val info = if (Build.VERSION.SDK_INT >= 33)
            activity.packageManager.getPackageArchiveInfo(berkas.path, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") activity.packageManager.getPackageArchiveInfo(berkas.path, 0)
        requireNotNull(info) { "Berkas unduhan bukan APK yang valid" }
        require(info.packageName == activity.packageName) { "APK bukan untuk aplikasi SIMPATIK" }
        @Suppress("DEPRECATION")
        val kodeBaru = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        @Suppress("DEPRECATION")
        val kodeLama = paketTerpasang().let { if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else it.versionCode.toLong() }
        require(kodeBaru > kodeLama) { "APK di server (versi $kodeBaru) tidak lebih baru dari aplikasi ini ($kodeLama)" }
        require(kodeBaru == versionCodeServer) { "Versi APK ($kodeBaru) berbeda dari keterangan server ($versionCodeServer)" }
    }

    private fun pasang(berkas: File) {
        if (!bolehMemasang()) {
            menungguIzin = berkas
            status("izin")
            activity.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}"))
            )
            return
        }
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.pembaruan", berkas)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        status("memasang")
        activity.startActivity(intent)
    }

    /** Dipanggil dari onResume: lanjutkan otomatis setelah izin diberikan. */
    fun lanjutkanSetelahIzin() {
        val berkas = menungguIzin ?: return
        if (!berkas.exists()) { menungguIzin = null; return }
        if (bolehMemasang()) {
            menungguIzin = null
            pasang(berkas)
        } else {
            menungguIzin = null
            status("izin-ditolak")
        }
    }
}

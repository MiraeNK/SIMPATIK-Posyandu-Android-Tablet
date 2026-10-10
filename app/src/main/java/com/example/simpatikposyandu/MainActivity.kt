package com.example.simpatikposyandu

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.simpatikposyandu.ui.theme.SIMPATIKPosyanduTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

import androidx.activity.OnBackPressedCallback
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var webViewInstance: WebView? = null
    private var lastBackPressTime: Long = 0L
    private var queueSpeaker: TextToSpeech? = null
    @Volatile private var queueSpeakerReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        queueSpeaker = TextToSpeech(applicationContext) { status ->
            queueSpeakerReady = status == TextToSpeech.SUCCESS
            if (queueSpeakerReady) {
                val languageResult = queueSpeaker?.setLanguage(Locale.forLanguageTag("id-ID"))
                queueSpeakerReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                queueSpeaker?.setSpeechRate(0.88f)
                queueSpeaker?.setPitch(1.0f)
            }
        }

        // Handle Tombol Back Hardware / Gesture Android
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val wv = webViewInstance
                if (wv != null) {
                    wv.evaluateJavascript("window.handleAndroidBack ? window.handleAndroidBack() : 'unhandled'") { result ->
                        val res = result?.replace("\"", "")?.trim()
                        if (res != "handled") {
                            // Jika berada di layar utama (Portal atau Login), konfirmasi 2x untuk keluar
                            val now = System.currentTimeMillis()
                            if (now - lastBackPressTime < 2000) {
                                finish()
                            } else {
                                lastBackPressTime = now
                                Toast.makeText(
                                    this@MainActivity,
                                    "Tekan sekali lagi untuk keluar dari aplikasi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    finish()
                }
            }
        })

        setContent {
            SIMPATIKPosyanduTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PosyanduWebView(
                        modifier = Modifier.padding(innerPadding),
                        activity = this,
                        onWebViewCreated = { wv -> webViewInstance = wv }
                    )
                }
            }
        }
    }

    fun announceQueue(queueNumber: String, childName: String): Boolean {
        val safeNumber = queueNumber.trim().uppercase(Locale.ROOT)
        // Nama sasaran sering tersimpan kapital semua; beberapa mesin TTS
        // menganggap kapital sebagai singkatan lalu mengeja setiap huruf.
        val safeName = childName.trim().replace(Regex("\\s+"), " ")
            .lowercase(Locale.forLanguageTag("id-ID"))
        if (safeNumber.isEmpty() || safeName.isEmpty()) return false
        if (!queueSpeakerReady) {
            runOnUiThread {
                Toast.makeText(this, "Suara antrean belum siap. Coba tekan Panggil Lagi.", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        val digits = safeNumber.removePrefix("A").map { it.toString() }.joinToString(" ")
        val announcement = "Nomor antrean A $digits. Atas nama $safeName. Silakan menuju meja pengukuran."
        runOnUiThread {
            queueSpeaker?.speak(
                announcement,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "queue_${System.currentTimeMillis()}"
            )
        }
        return true
    }

    override fun onDestroy() {
        queueSpeaker?.stop()
        queueSpeaker?.shutdown()
        queueSpeaker = null
        queueSpeakerReady = false
        super.onDestroy()
    }
}

private class PosyanduDatabase(context: Context) :
    SQLiteOpenHelper(context, "simpatik_posyandu.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE measurements (
                id_pengukuran TEXT PRIMARY KEY,
                nik TEXT NOT NULL,
                id_periode TEXT NOT NULL,
                tanggal_ukur TEXT NOT NULL,
                payload_json TEXT NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'pending',
                revision INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                UNIQUE(nik, id_periode)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE children (
                nik TEXT PRIMARY KEY,
                payload_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        createServiceQueueTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createServiceQueueTable(db)
    }

    private fun createServiceQueueTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS service_queue (
                queue_id TEXT PRIMARY KEY,
                service_date TEXT NOT NULL,
                payload_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    fun upsertMeasurement(payloadText: String): JSONObject {
        val payload = JSONObject(payloadText)
        val id = payload.optString("id_pengukuran").trim()
        val nik = payload.optString("nik").trim()
        val period = payload.optString("id_periode").trim()
        val date = payload.optString("tanggal_ukur").trim()
        require(id.isNotEmpty() && nik.isNotEmpty() && period.isNotEmpty() && date.isNotEmpty()) {
            "ID pengukuran, NIK, periode, dan tanggal ukur wajib diisi"
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val parsedDate = dateFormat.parse(date) ?: error("Tanggal pengukuran tidak valid")
        require(!parsedDate.after(Date())) { "Tanggal pengukuran tidak boleh berada di masa depan" }
        val dateParts = date.split("-")
        require(dateParts.size == 3 && period == "periode_${dateParts[0]}_${dateParts[1].toInt()}") {
            "Periode tidak sesuai dengan tanggal pengukuran"
        }
        fun number(field: String, required: Boolean): Double? {
            val raw = payload.optString(field).trim()
            if (!required && (raw.isEmpty() || raw == "N/A")) return null
            return raw.replace(',', '.').toDoubleOrNull()
                ?: throw IllegalArgumentException("$field harus berupa angka")
        }
        fun validateRange(label: String, value: Double?, range: ClosedFloatingPointRange<Double>) {
            require(value == null || value in range) { "$label berada di luar rentang wajar" }
        }
        validateRange("Berat badan", number("bb_kg", true), 1.0..40.0)
        validateRange("Panjang/tinggi badan", number("panjang_tinggi_cm", true), 30.0..130.0)
        validateRange("LILA", number("lila", false), 8.0..25.0)
        validateRange("LIKA", number("lika", false), 30.0..60.0)

        val db = writableDatabase
        var revision = 1
        var createdAt = System.currentTimeMillis()
        var action = "inserted"
        db.query(
            "measurements",
            arrayOf("revision", "created_at"),
            "id_pengukuran = ? OR (nik = ? AND id_periode = ?)",
            arrayOf(id, nik, period), null, null, null, "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                revision = cursor.getInt(0) + 1
                createdAt = cursor.getLong(1)
                action = "updated"
            }
        }

        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("id_pengukuran", id)
            put("nik", nik)
            put("id_periode", period)
            put("tanggal_ukur", date)
            put("payload_json", payload.toString())
            put("sync_status", "pending")
            put("revision", revision)
            put("created_at", createdAt)
            put("updated_at", now)
        }
        db.beginTransaction()
        try {
            // Satu balita hanya memiliki satu catatan aktif per periode. Koreksi mengganti baris itu.
            db.delete("measurements", "nik = ? AND id_periode = ? AND id_pengukuran <> ?", arrayOf(nik, period, id))
            db.insertWithOnConflict("measurements", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return JSONObject().put("ok", true).put("action", action).put("revision", revision).put("id_pengukuran", id)
    }

    fun pendingMeasurements(): JSONArray {
        val rows = JSONArray()
        readableDatabase.query(
            "measurements", arrayOf("payload_json"), "sync_status = ?",
            arrayOf("pending"), null, null, "updated_at ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) rows.put(JSONObject(cursor.getString(0)))
        }
        return rows
    }

    fun markSynced(id: String): Boolean {
        val values = ContentValues().apply { put("sync_status", "synced") }
        return writableDatabase.update("measurements", values, "id_pengukuran = ?", arrayOf(id)) > 0
    }

    fun replaceChildren(jsonText: String): Int {
        val children = JSONArray(jsonText)
        val db = writableDatabase
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            db.delete("children", null, null)
            for (index in 0 until children.length()) {
                val child = children.getJSONObject(index)
                val nik = child.optString("nik", child.optString("id")).trim()
                if (nik.isEmpty()) continue
                val values = ContentValues().apply {
                    put("nik", nik)
                    put("payload_json", child.toString())
                    put("updated_at", now)
                }
                db.insertWithOnConflict("children", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return children.length()
    }

    fun children(): JSONArray {
        val rows = JSONArray()
        readableDatabase.query("children", arrayOf("payload_json"), null, null, null, null, "updated_at ASC").use { cursor ->
            while (cursor.moveToNext()) rows.put(JSONObject(cursor.getString(0)))
        }
        return rows
    }

    fun replaceServiceQueue(jsonText: String): Int {
        val queue = JSONArray(jsonText)
        val db = writableDatabase
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            db.delete("service_queue", null, null)
            for (index in 0 until queue.length()) {
                val item = queue.getJSONObject(index)
                val id = item.optString("id").trim()
                val date = item.optString("date").trim()
                if (id.isEmpty() || date.isEmpty()) continue
                val values = ContentValues().apply {
                    put("queue_id", id)
                    put("service_date", date)
                    put("payload_json", item.toString())
                    put("updated_at", now)
                }
                db.insertWithOnConflict("service_queue", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return queue.length()
    }

    fun serviceQueue(): JSONArray {
        val rows = JSONArray()
        readableDatabase.query(
            "service_queue", arrayOf("payload_json"), null, null, null, null,
            "service_date ASC, updated_at ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) rows.put(JSONObject(cursor.getString(0)))
        }
        return rows
    }
}

// Jembatan antara JavaScript (Vue/HTML) dan penyimpanan SQLite Android.
@Keep
class AndroidAppBridge(
    private val activity: MainActivity,
    private val webViewProvider: () -> WebView?
) {
    private val database = PosyanduDatabase(activity.applicationContext)

    @JavascriptInterface
    fun simpanDataPengukuran(jsonData: String): String = try {
        database.upsertMeasurement(jsonData).toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal menyimpan pengukuran", error)
        JSONObject().put("ok", false).put("error", error.message ?: "Gagal menyimpan data").toString()
    }

    @JavascriptInterface
    fun ambilPengukuranTertunda(): String = try {
        database.pendingMeasurements().toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal membaca antrean sinkronisasi", error)
        "[]"
    }

    @JavascriptInterface
    fun tandaiPengukuranTersinkron(idPengukuran: String): Boolean = database.markSynced(idPengukuran)

    @JavascriptInterface
    fun simpanDaftarAnak(jsonData: String): String = try {
        val count = database.replaceChildren(jsonData)
        JSONObject().put("ok", true).put("count", count).toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal menyimpan daftar anak", error)
        JSONObject().put("ok", false).put("error", error.message ?: "Gagal menyimpan data anak").toString()
    }

    @JavascriptInterface
    fun ambilDaftarAnak(): String = try {
        database.children().toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal membaca daftar anak", error)
        "[]"
    }

    @JavascriptInterface
    fun simpanAntrean(jsonData: String): String = try {
        val count = database.replaceServiceQueue(jsonData)
        JSONObject().put("ok", true).put("count", count).toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal menyimpan antrean layanan", error)
        JSONObject().put("ok", false).put("error", error.message ?: "Gagal menyimpan antrean").toString()
    }

    @JavascriptInterface
    fun ambilAntrean(): String = try {
        database.serviceQueue().toString()
    } catch (error: Exception) {
        Log.e("PosyanduDatabase", "Gagal membaca antrean layanan", error)
        "[]"
    }

    @JavascriptInterface
    fun scanKartuSasaran(): String {
        activity.runOnUiThread {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()
            GmsBarcodeScanning.getClient(activity, options)
                .startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue.orEmpty().trim()
                    if (value.isNotEmpty()) {
                        webViewProvider()?.evaluateJavascript(
                            "window.handleScanKartuSasaran && window.handleScanKartuSasaran(${JSONObject.quote(value)})",
                            null
                        )
                    }
                }
                .addOnCanceledListener {
                    webViewProvider()?.evaluateJavascript(
                        "window.handleScanKartuDibatalkan && window.handleScanKartuDibatalkan()",
                        null
                    )
                }
                .addOnFailureListener { error ->
                    Log.e("SasaranScanner", "Pemindaian kartu gagal", error)
                    webViewProvider()?.evaluateJavascript(
                        "window.handleScanKartuGagal && window.handleScanKartuGagal(${JSONObject.quote(error.message ?: "Pemindaian kartu gagal")})",
                        null
                    )
                }
        }
        return JSONObject().put("ok", true).put("status", "started").toString()
    }

    @JavascriptInterface
    fun tampilkanPesan(message: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun ucapkanPanggilan(queueNumber: String, childName: String): Boolean =
        activity.announceQueue(queueNumber, childName)

    @JavascriptInterface
    fun dapatkanInfoJaringan(): String {
        return try {
            val wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = wifiManager?.dhcpInfo
            val gatewayIp = if (dhcp != null && dhcp.gateway != 0) {
                String.format(
                    Locale.US, "%d.%d.%d.%d",
                    dhcp.gateway and 0xff,
                    dhcp.gateway shr 8 and 0xff,
                    dhcp.gateway shr 16 and 0xff,
                    dhcp.gateway shr 24 and 0xff
                )
            } else ""
            val myIp = if (dhcp != null && dhcp.ipAddress != 0) {
                String.format(
                    Locale.US, "%d.%d.%d.%d",
                    dhcp.ipAddress and 0xff,
                    dhcp.ipAddress shr 8 and 0xff,
                    dhcp.ipAddress shr 16 and 0xff,
                    dhcp.ipAddress shr 24 and 0xff
                )
            } else ""
            JSONObject().apply {
                put("ok", true)
                put("ipTablet", myIp)
                put("gateway", gatewayIp)
            }.toString()
        } catch (e: Exception) {
            JSONObject().put("ok", false).put("error", e.message ?: "Gagal membaca info jaringan").toString()
        }
    }

    @JavascriptInterface
    fun temukanServer() {
        Thread {
            try {
                val wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val multicastLock = wifiManager?.createMulticastLock("SimpatikDiscovery")?.apply {
                    setReferenceCounted(true)
                    acquire()
                }

                var serverFoundJson: String? = null

                // 1. Coba UDP Broadcast ke port 43210 (Auto-Discovery)
                try {
                    val socket = DatagramSocket().apply {
                        broadcast = true
                        soTimeout = 1200
                    }
                    val probeData = "SIMPATIK_DISCOVER_SERVER".toByteArray(Charsets.UTF_8)
                    val broadcastAddr = InetAddress.getByName("255.255.255.255")
                    val packet = DatagramPacket(probeData, probeData.size, broadcastAddr, 43210)
                    socket.send(packet)

                    val buffer = ByteArray(2048)
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)

                    val responseText = String(receivePacket.data, 0, receivePacket.length, Charsets.UTF_8)
                    val senderIp = receivePacket.address.hostAddress ?: ""
                    socket.close()

                    if (responseText.contains("simpatik-posyandu") && senderIp.isNotEmpty()) {
                        val obj = JSONObject(responseText)
                        val port = obj.optInt("portaApi", 4321)
                        val path = obj.optString("jalurApi", "/api/v1")
                        val apiUrl = "http://$senderIp:$port$path"
                        serverFoundJson = JSONObject().apply {
                            put("ok", true)
                            put("ip", senderIp)
                            put("port", port)
                            put("apiUrl", apiUrl)
                            put("metode", "udp_broadcast")
                        }.toString()
                    }
                } catch (e: Exception) {
                    Log.d("Discovery", "UDP broadcast note: ${e.message}")
                } finally {
                    try { multicastLock?.release() } catch (_: Exception) {}
                }

                // 2. Jika UDP broadcast belum membuahkan hasil (misal AP isolation/multicast diblokir), coba probe Gateway & IP hotspot umum
                if (serverFoundJson == null) {
                    val candidates = mutableListOf<String>()

                    val dhcp = wifiManager?.dhcpInfo
                    if (dhcp != null && dhcp.gateway != 0) {
                        val gatewayIp = String.format(
                            Locale.US, "%d.%d.%d.%d",
                            dhcp.gateway and 0xff,
                            dhcp.gateway shr 8 and 0xff,
                            dhcp.gateway shr 16 and 0xff,
                            dhcp.gateway shr 24 and 0xff
                        )
                        if (gatewayIp != "0.0.0.0") candidates.add(gatewayIp)
                    }

                    // Tambahkan IP host hotspot populer
                    candidates.add("192.168.43.1")  // Hotspot Android
                    candidates.add("192.168.137.1") // Hotspot Windows
                    candidates.add("172.20.10.1")   // Hotspot iOS
                    candidates.add("192.168.1.1")   // Router bawaan
                    candidates.add("192.168.0.1")

                    for (ip in candidates.distinct()) {
                        try {
                            val testUrl = "http://$ip:4321/api/v1/kesehatan"
                            val connection = (URL(testUrl).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 400
                                readTimeout = 400
                                requestMethod = "GET"
                            }
                            if (connection.responseCode == 200) {
                                val stream = connection.inputStream.bufferedReader()
                                val body = stream.readText()
                                stream.close()
                                if (body.contains("siap")) {
                                    val apiUrl = "http://$ip:4321/api/v1"
                                    serverFoundJson = JSONObject().apply {
                                        put("ok", true)
                                        put("ip", ip)
                                        put("port", 4321)
                                        put("apiUrl", apiUrl)
                                        put("metode", "gateway_probe")
                                    }.toString()
                                    break
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Kirim hasil kembali ke WebView
                activity.runOnUiThread {
                    if (serverFoundJson != null) {
                        webViewProvider()?.evaluateJavascript(
                            "window.handleServerDitemukan && window.handleServerDitemukan(${JSONObject.quote(serverFoundJson)})",
                            null
                        )
                    } else {
                        webViewProvider()?.evaluateJavascript(
                            "window.handleServerGagal && window.handleServerGagal('Tidak dapat menemukan server secara otomatis. Pastikan PC dan tablet terhubung ke Hotspot/Wi-Fi yang sama.')",
                            null
                        )
                    }
                }
            } catch (err: Exception) {
                Log.e("Discovery", "Gagal auto-discovery", err)
                activity.runOnUiThread {
                    webViewProvider()?.evaluateJavascript(
                        "window.handleServerGagal && window.handleServerGagal(${JSONObject.quote(err.message ?: "Galat pencarian server")})",
                        null
                    )
                }
            }
        }.start()
    }
}

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PosyanduWebView(
    modifier: Modifier = Modifier,
    activity: MainActivity,
    onWebViewCreated: ((WebView) -> Unit)? = null
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                onWebViewCreated?.invoke(this)
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true // Penting untuk Vue.js
                    databaseEnabled = true
                    allowFileAccess = true // Wajib di Android 11+ (API 30+) agar file:///android_asset dapat diakses
                    allowContentAccess = true
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
                
                WebView.setWebContentsDebuggingEnabled(true)
                
                // Mencegah link terbuka di browser luar & menangkap log error
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("PosyanduWebView", "Halaman selesai dimuat: $url")
                    }
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e("PosyanduWebView", "Error loading ${request?.url}: ${error?.description}")
                    }
                }
                
                // Menangkap message console.log / error dari Vue / JavaScript
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(
                            "PosyanduJSConsole",
                            "${consoleMessage?.message()} -- Line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}"
                        )
                        return true
                    }

                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?
                    ): Boolean {
                        Toast.makeText(activity, message ?: "", Toast.LENGTH_SHORT).show()
                        result?.confirm()
                        return true
                    }
                }
                
                // Mendaftarkan objek 'AndroidBridge' ke dalam window Javascript
                addJavascriptInterface(AndroidAppBridge(activity) { this }, "AndroidBridge")
                
                // Memuat file HTML kita yang ada di folder assets
                loadUrl("file:///android_asset/index.html")
            }
        }
    )
}

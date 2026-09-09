package com.example.simpatikposyandu

import android.annotation.SuppressLint
import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SIMPATIKPosyanduTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PosyanduWebView(
                        modifier = Modifier.padding(innerPadding),
                        activity = this
                    )
                }
            }
        }
    }
}

// Jembatan antara JavaScript (Vue/HTML) dan Kotlin
@Keep
class AndroidAppBridge(private val activity: ComponentActivity) {
    
    // Anotasi ini wajib agar fungsi bisa dipanggil dari HTML/JS
    @JavascriptInterface
    fun simpanDataPengukuran(jsonData: String) {
        // Nanti di sini kita akan parsing JSON dan menyimpannya ke Room Database!
        // Sementara kita tampilkan notifikasi toast
        activity.runOnUiThread {
            Toast.makeText(activity, "Data diterima di Android!\n$jsonData", Toast.LENGTH_LONG).show()
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PosyanduWebView(modifier: Modifier = Modifier, activity: ComponentActivity) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
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
                addJavascriptInterface(AndroidAppBridge(activity), "AndroidBridge")
                
                // Memuat file HTML kita yang ada di folder assets
                loadUrl("file:///android_asset/index.html")
            }
        }
    )
}

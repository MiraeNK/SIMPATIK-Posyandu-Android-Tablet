package com.example.simpatikposyandu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Native camera scanner: usable even when file:// WebView has no getUserMedia. */
class CardScannerActivity : ComponentActivity() {
    private lateinit var preview: PreviewView
    private lateinit var message: TextView
    private lateinit var front: Button
    private lateinit var back: Button
    private var provider: ProcessCameraProvider? = null
    private var lens = CameraSelector.LENS_FACING_FRONT
    @Volatile private var generation = 0
    private val busy = AtomicBoolean(false)
    private val delivered = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor()
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) prepareCamera() else {
            message.text = "Izin kamera belum diberikan. Ketuk Izinkan kamera atau gunakan pencarian manual."
            permissionButton.visibility = View.VISIBLE
        }
    }
    private lateinit var permissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("scanner_camera", MODE_PRIVATE)
        lens = savedInstanceState?.getInt("lens")
            ?: prefs.getInt("lens", if (intent.getStringExtra("facing") == "environment")
                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(17, 24, 39))
            setPadding(dp(16), dp(12), dp(16), dp(16))
        }
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(dp(16) + insets.systemWindowInsetLeft, dp(12) + insets.systemWindowInsetTop,
                dp(16) + insets.systemWindowInsetRight, dp(16) + insets.systemWindowInsetBottom)
            insets
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "Pindai kartu sasaran"; textSize = 21f; setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(Button(this).apply {
            text = "Tutup"; minHeight = dp(48); setOnClickListener { finish() }
        })
        root.addView(header)
        message = TextView(this).apply {
            text = "Menyiapkan kamera…"; textSize = 16f; setTextColor(Color.WHITE)
            setPadding(0, dp(12), 0, dp(12)); gravity = Gravity.CENTER
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        root.addView(message)
        preview = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(TextView(this).apply {
            text = "Arahkan QR kartu ke kamera. Pilihan kamera terakhir disimpan otomatis."
            textSize = 16f; setTextColor(Color.WHITE)
            setPadding(0, dp(16), 0, dp(8)); gravity = Gravity.CENTER
        })
        val controls = LinearLayout(this)
        front = Button(this).apply { text = "Kamera Depan"; setOnClickListener { selectCamera(CameraSelector.LENS_FACING_FRONT) } }
        back = Button(this).apply { text = "Kamera Belakang"; setOnClickListener { selectCamera(CameraSelector.LENS_FACING_BACK) } }
        for (button in listOf(front, back)) {
            button.textSize = 16f
            button.isAllCaps = false
            button.minHeight = dp(64)
            button.isEnabled = false
            controls.addView(button, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(controls)
        permissionButton = Button(this).apply {
            text = "Izinkan kamera"; visibility = View.GONE
            setOnClickListener { permission.launch(Manifest.permission.CAMERA) }
        }
        root.addView(permissionButton)
        setContentView(root)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            prepareCamera()
        else permission.launch(Manifest.permission.CAMERA)
    }

    private fun prepareCamera() {
        permissionButton.visibility = View.GONE
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            if (isFinishing || isDestroyed) return@addListener
            try {
                provider = future.get()
                val cameraProvider = provider!!
                front.isEnabled = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                back.isEnabled = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                if (!front.isEnabled) front.text = "Depan tidak tersedia"
                if (!back.isEnabled) back.text = "Belakang tidak tersedia"
                if (!cameraProvider.hasCamera(selector(lens))) {
                    lens = if (front.isEnabled) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                }
                bindCamera()
            } catch (error: Exception) {
                message.text = "Kamera tidak dapat dibuka. Tutup pemindai dan coba lagi."
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun selector(value: Int) = CameraSelector.Builder().requireLensFacing(value).build()

    private fun selectCamera(value: Int) {
        if (value == lens) return
        val previous = lens
        lens = value
        if (!bindCamera()) {
            lens = previous
            bindCamera()
            message.text = "Kamera pilihan tidak dapat dibuka. Menggunakan kamera sebelumnya."
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun bindCamera(): Boolean {
        val cameraProvider = provider ?: return false
        val activeGeneration = ++generation
        return try {
            cameraProvider.unbindAll()
            val cameraPreview = Preview.Builder().build().also { it.setSurfaceProvider(preview.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            analysis.setAnalyzer(executor) { frame ->
                val image = frame.image
                if (image == null || delivered.get() || activeGeneration != generation || !busy.compareAndSet(false, true)) {
                    frame.close()
                } else {
                    try {
                        val input = InputImage.fromMediaImage(image, frame.imageInfo.rotationDegrees)
                        scanner.process(input)
                            .addOnSuccessListener { codes ->
                                if (activeGeneration == generation && !isFinishing && !isDestroyed) {
                                    codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.trim()?.let { value ->
                                        if (delivered.compareAndSet(false, true)) {
                                            setResult(RESULT_OK, Intent().putExtra("qr", value))
                                            finish()
                                        }
                                    }
                                }
                            }
                            .addOnCompleteListener { frame.close(); busy.set(false) }
                    } catch (error: Exception) { frame.close(); busy.set(false) }
                }
            }
            cameraProvider.bindToLifecycle(this, selector(lens), cameraPreview, analysis)
            getSharedPreferences("scanner_camera", MODE_PRIVATE).edit().putInt("lens", lens).apply()
            front.isSelected = lens == CameraSelector.LENS_FACING_FRONT
            back.isSelected = lens == CameraSelector.LENS_FACING_BACK
            for (button in listOf(front, back)) {
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (button.isSelected) Color.rgb(0, 112, 73) else Color.rgb(55, 65, 81))
                button.setTextColor(Color.WHITE)
                button.contentDescription = button.text.toString() + if (button.isSelected) ", aktif" else ""
            }
            message.text = if (lens == CameraSelector.LENS_FACING_FRONT) "Kamera depan aktif" else "Kamera belakang aktif"
            true
        } catch (error: Exception) {
            message.text = "Kamera tidak dapat dibuka. Pilih kamera lain atau tutup pemindai."
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("lens", lens)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        generation++
        provider?.unbindAll()
        executor.shutdown()
        scanner.close()
        super.onDestroy()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
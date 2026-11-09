package com.example.simpatikposyandu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardScannerDeviceTest {
    @Test fun cameraSelectionSurvivesReopenAndRecreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("pm grant " + context.packageName + " " + Manifest.permission.CAMERA).close()
        val prefs = context.getSharedPreferences("scanner_camera", Context.MODE_PRIVATE)
        val old = if (prefs.contains("lens")) prefs.getInt("lens", -1) else null
        prefs.edit().remove("lens").commit()
        fun launch() = ActivityScenario.launch<CardScannerActivity>(
            Intent(context, CardScannerActivity::class.java).putExtra("facing", "user"))
        fun waitCamera(scenario: ActivityScenario<CardScannerActivity>, expected: Int) {
            val deadline = SystemClock.elapsedRealtime() + 20000
            while (SystemClock.elapsedRealtime() < deadline) {
                var ready = false
                scenario.onActivity { activity ->
                    val root = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
                    fun buttons(v: android.view.View): List<Button> = when(v) {
                        is Button -> listOf(v)
                        is android.view.ViewGroup -> (0 until v.childCount).flatMap { buttons(v.getChildAt(it)) }
                        else -> emptyList()
                    }
                    val label = if (expected == CameraSelector.LENS_FACING_FRONT) "Kamera Depan" else "Kamera Belakang"
                    ready = buttons(root).any { it.text.toString() == label && it.isEnabled && it.isSelected }
                }
                if (ready) return
                SystemClock.sleep(200)
            }
            fail("Kamera pilihan tidak aktif atau tombol tidak tersedia")
        }
        try {
            launch().use { scenario ->
                waitCamera(scenario, CameraSelector.LENS_FACING_FRONT)
                scenario.onActivity { activity ->
                    fun clickBack(v: android.view.View) {
                        if (v is Button && v.text.toString() == "Kamera Belakang") v.performClick()
                        else if (v is android.view.ViewGroup) (0 until v.childCount).forEach { clickBack(v.getChildAt(it)) }
                    }
                    clickBack(activity.findViewById(android.R.id.content))
                }
                waitCamera(scenario, CameraSelector.LENS_FACING_BACK)
                assertEquals(CameraSelector.LENS_FACING_BACK, prefs.getInt("lens", -1))
                scenario.recreate()
                waitCamera(scenario, CameraSelector.LENS_FACING_BACK)
            }
            // Even an older WebView preference for front must not overwrite the last native choice.
            launch().use { scenario -> waitCamera(scenario, CameraSelector.LENS_FACING_BACK) }
        } finally {
            if (old == null) prefs.edit().remove("lens").commit() else prefs.edit().putInt("lens",old).commit()
        }
    }
}
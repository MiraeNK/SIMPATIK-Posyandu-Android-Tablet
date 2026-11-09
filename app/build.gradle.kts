import java.util.Properties

// Kunci release disimpan di luar repo. Bawaan: ~/.simpatik-signing/keystore.properties
// (storeFile, storePassword, keyAlias, keyPassword). Bisa diganti lewat env SIMPATIK_KEYSTORE_PROPERTIES.
val propertiKunciRilis = Properties().apply {
    val berkas = file(
        System.getenv("SIMPATIK_KEYSTORE_PROPERTIES")
            ?: "${System.getProperty("user.home")}/.simpatik-signing/keystore.properties"
    )
    if (berkas.exists()) berkas.inputStream().use { load(it) }
}

// Alamat server dibaca dari server.properties di akar proyek (tidak masuk repo).
// Contoh isi: lihat server.properties.example.
val propertiServer = Properties().apply {
    val berkas = rootProject.file("server.properties")
    if (berkas.exists()) berkas.inputStream().use { load(it) }
}
fun nilaiServer(kunci: String): String =
    (System.getenv("SIMPATIK_" + kunci.uppercase()) ?: propertiServer.getProperty(kunci) ?: "")
        .trim().replace("\\", "\\\\").replace("\"", "\\\"")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.simpatikposyandu"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Nama paket rilis; namespace kode tetap com.example.simpatikposyandu.
        applicationId = "id.simpatik.posyandu"
        minSdk = 24
        targetSdk = 37
        versionCode = 38
        versionName = "1.15.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "PORTAL_API_URL", "\"${nilaiServer("portalApiUrl")}\"")
        buildConfigField("String", "TAILSCALE_API_URL", "\"${nilaiServer("tailscaleApiUrl")}\"")
    }

    signingConfigs {
        create("release") {
            if (propertiKunciRilis.getProperty("storeFile") != null) {
                storeFile = file(propertiKunciRilis.getProperty("storeFile"))
                storePassword = propertiKunciRilis.getProperty("storePassword")
                keyAlias = propertiKunciRilis.getProperty("keyAlias")
                keyPassword = propertiKunciRilis.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

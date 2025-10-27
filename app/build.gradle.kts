plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.company.lanzamientos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.company.lanzamientos"
        minSdk = 26            // requerido por Apache POI
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("RELEASE_KEYSTORE") ?: "release.keystore")
            storePassword = (project.findProperty("RELEASE_KEYSTORE_PASSWORD") ?: "") as String
            keyAlias = (project.findProperty("RELEASE_KEY_ALIAS") ?: "") as String
            keyPassword = (project.findProperty("RELEASE_KEY_PASSWORD") ?: "") as String
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            // proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isMinifyEnabled = false }
    }
}

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Si no tenés proguard-rules.pro, dejalo comentado para evitar error.
            // proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Forzar toolchain Java 17 para Kotlin (kapt y compilación)
kotlin {
    jvmToolchain(17)
}

// Opcional pero recomendable para Room/kapt
kapt {
    correctErrorTypes = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.apache.poi:poi-ooxml:5.2.5") {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")}

    // Tabs / navegación
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Lifecycle / Activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.2")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Apache POI para .xlsx  (excluimos log4j para achicar y evitar arrastre)
    implementation("org.apache.poi:poi:5.2.5") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    implementation("org.apache.poi:poi-ooxml-lite:5.2.5")
}

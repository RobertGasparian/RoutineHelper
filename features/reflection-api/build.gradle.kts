plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.robertgasparian.routinehelper.features.reflection.api"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
}

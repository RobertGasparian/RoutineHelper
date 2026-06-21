plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.robertgasparian.routinehelper.libs.routine.snapshot.data"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
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
    api(project(":libs:routine:snapshot:domain"))
    implementation(project(":libs:routine:template:domain"))

    implementation(libs.androidx.room.ktx)
    api(libs.androidx.room.runtime)
    implementation(libs.hilt.android)
    api(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)
}

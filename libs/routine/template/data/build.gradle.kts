plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.robertgasparian.routinehelper.libs.routine.template.data"
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
    api(project(":core:time"))
    api(project(":libs:routine:template:domain"))

    implementation(libs.androidx.room.ktx)
    api(libs.androidx.room.runtime)
    api(libs.kotlinx.coroutines.core)
}

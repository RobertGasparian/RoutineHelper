plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.robertgasparian.routinehelper.libs.routine.tracking.data"
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
    api(project(":core:time"))
    implementation(project(":libs:routine:reflection:domain"))
    api(project(":libs:routine:template:data"))
    implementation(project(":libs:routine:template:domain"))
    api(project(":libs:routine:tracking:domain"))

    api(libs.androidx.room.runtime)
    implementation(libs.hilt.android)
    api(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))
}

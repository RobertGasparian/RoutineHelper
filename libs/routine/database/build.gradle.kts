plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.robertgasparian.routinehelper.libs.routine.database"
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
    api(project(":libs:routine:current-list:data"))
    api(project(":libs:routine:reflection:data"))
    api(project(":libs:routine:template:data"))
    api(project(":libs:routine:tracking:data"))
    api(project(":libs:routine:snapshot:data"))

    implementation(libs.hilt.android)
    api(libs.androidx.room.runtime)

    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}

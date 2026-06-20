plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.robertgasparian.routinehelper.libs.routine.database"
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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(project(":libs:routine:template:data"))
    api(project(":libs:routine:tracking:data"))
    api(project(":libs:routine:snapshot:data"))

    implementation(libs.hilt.android)
    api(libs.androidx.room.runtime)

    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.robertgasparian.routinehelper.features.routinetracking"
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

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(project(":core:presentation"))
    implementation(project(":core:time"))
    implementation(project(":core:ui"))
    api(project(":features:reflection-api"))
    implementation(project(":libs:routine:reflection:domain"))
    implementation(project(":libs:routine:snapshot:domain"))
    implementation(project(":libs:routine:removal:domain"))
    implementation(project(":libs:routine:template:domain"))
    implementation(project(":libs:routine:tracking:domain"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))
    testImplementation(testFixtures(project(":libs:routine:snapshot:domain")))
    testImplementation(testFixtures(project(":libs:routine:removal:domain")))
    testImplementation(testFixtures(project(":libs:routine:template:domain")))
    testImplementation(testFixtures(project(":libs:routine:tracking:domain")))
    testImplementation(testFixtures(project(":libs:routine:reflection:domain")))
}

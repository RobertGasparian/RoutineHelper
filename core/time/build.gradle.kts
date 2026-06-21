plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.dagger.runtime)
    implementation(libs.hilt.core)
    implementation(libs.javax.inject)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}

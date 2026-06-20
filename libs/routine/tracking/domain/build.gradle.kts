plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `java-test-fixtures`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":libs:routine:template:domain"))

    implementation(libs.dagger.runtime)
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    testFixturesImplementation(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)
}

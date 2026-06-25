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
    api(project(":core:time"))

    api(project(":libs:routine:template:domain"))
    api(project(":libs:routine:tracking:domain"))

    implementation(libs.dagger.runtime)
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))
    testImplementation(testFixtures(project(":libs:routine:tracking:domain")))

    testFixturesImplementation(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)
}

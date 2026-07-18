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
    implementation(project(":libs:routine:current-list:domain"))
    implementation(project(":libs:routine:template:domain"))

    implementation(libs.dagger.runtime)
    implementation(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":libs:routine:current-list:domain")))
    testImplementation(testFixtures(project(":libs:routine:template:domain")))

    testFixturesImplementation(libs.kotlinx.coroutines.core)
}

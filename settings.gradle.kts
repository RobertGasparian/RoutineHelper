pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RoutineHelper"
include(":app")
include(":core:time")
include(":core:ui")
include(":libs:routine:template:domain")
include(":libs:routine:template:data")
include(":libs:routine:tracking:domain")
include(":libs:routine:tracking:data")

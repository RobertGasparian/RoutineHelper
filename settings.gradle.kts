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
include(":core:presentation")
include(":core:ui")
include(":core:testing")
include(":libs:routine:template:domain")
include(":libs:routine:template:data")
include(":libs:routine:current-list:domain")
include(":libs:routine:current-list:data")
include(":libs:routine:removal:domain")
include(":libs:routine:tracking:domain")
include(":libs:routine:tracking:data")
include(":libs:routine:snapshot:domain")
include(":libs:routine:snapshot:data")
include(":libs:routine:database")
include(":libs:settings:domain")
include(":libs:settings:data")
include(":features:routine-tracking")
include(":features:current-list")
include(":features:removal-undo")
include(":features:action-editor")
include(":features:history")
include(":features:settings")
include(":libs:background:work")

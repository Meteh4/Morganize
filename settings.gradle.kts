pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Morganize"

include(":app")

// Core modules
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:ui")

// Feature modules
include(":feature:onboarding")
include(":feature:list")
include(":feature:create")
include(":feature:edit")
include(":core:components")

pluginManagement {
    repositories {
        google() // Để mặc định như này là an toàn nhất
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FoddyApp"
include(":app")
 
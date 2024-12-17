pluginManagement {
    repositories {
        google()
        jcenter()  // 注意：jcenter() 已經被棄用，建議替換為 mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven { url = uri("https://jitpack.io") }  // 修正自定義 Maven 庫的語法
        google()
        jcenter()  // 注意：jcenter() 已經被棄用，建議替換為 mavenCentral()
    }
}

rootProject.name = "20240416restart"
include(":app")

buildscript {
    repositories {
        google()
        mavenCentral() // 替換掉 jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("com.google.gms:google-services:4.4.1") // 確保版本一致
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

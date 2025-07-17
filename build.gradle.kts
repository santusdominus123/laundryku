// In your project-level build.gradle.kts (not the app-level one)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0") // Use your current AGP version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22") // Use your current Kotlin version
        classpath("com.google.gms:google-services:4.3.15") // Add this line
    }
}
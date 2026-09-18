plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.nexo.waterquality"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nexo.waterquality"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "6.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
}

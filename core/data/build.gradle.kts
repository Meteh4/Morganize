plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.metoly.morganize.core.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))

    implementation(libs.androidx.room.ktx)
    implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
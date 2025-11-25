plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms)
}

android {
    namespace = "br.edu.fatecgru.glice"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.edu.fatecgru.glice"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation(libs.circleimageview)

    // FIREBASE
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.firebase:firebase-firestore:25.0.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Cloudinary
    // All:
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    // Download + Preprocess:
    implementation("com.cloudinary:cloudinary-android-download:3.0.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.0.2")
}



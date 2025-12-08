plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dolorders"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dolorders"
        minSdk = 29
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
    val lifecycleVersion = "2.8.3"
    val activityVersion = "1.11.0"
    val fragmentVersion = "1.8.1"
    val appCompatVersion = "1.7.0"
    val constraintLayoutVersion = "2.1.4"
    val materialVersion = "1.12.0"

    constraints {
        implementation("androidx.core:core-ktx:1.13.1") {
            because("Garantir une version unique du core de Kotlin")
        }
        implementation("androidx.appcompat:appcompat:$appCompatVersion") {
            because("Garantir une version unique d'appcompat")
        }
    }

    // App
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")
    implementation("androidx.activity:activity-ktx:$activityVersion")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("com.google.code.gson:gson:2.10.1")

    // Tests locaux JVM
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.espresso.intents)
    testImplementation(libs.junit)

    // Tests instrumentés (AndroidTest)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // FragmentScenario -> version compatible avec AndroidX Test 1.5.x
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
}

val signingPropertiesFile = rootProject.file("signing.properties")

android {
    namespace = "co.appreactor.news"
    compileSdk = 33

    defaultConfig {
        applicationId = "co.appreactor.news"
        minSdk = 29
        targetSdk = 33
        versionCode = 23
        versionName = "0.4.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "news-$versionName")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi"
    }

    signingConfigs {
        if (signingPropertiesFile.exists()) {
            create("release") {
                val signingProperties = Properties()
                signingProperties.load(FileInputStream(signingPropertiesFile))
                storeFile = File(signingProperties["releaseKeystoreFile"] as String)
                storePassword = signingProperties["releaseKeystorePassword"] as String
                keyAlias = signingProperties["releaseKeyAlias"] as String
                keyPassword = signingProperties["releaseKeyPassword"] as String
            }
        }

        create("selfSignedRelease") {
            storeFile = File(rootDir, "release.jks")
            storePassword = "news-android"
            keyAlias = "news-android"
            keyPassword = "news-android"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        if (signingPropertiesFile.exists()) {
            release {
                val signingProperties = Properties()
                signingProperties.load(FileInputStream(signingPropertiesFile))
                signingConfig = signingConfigs.getByName("release")
            }
        }

        create("selfSignedRelease") {
            signingConfig = signingConfigs.getByName("selfSignedRelease")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets.all {
        kotlin.srcDir("build/generated/ksp/$name/kotlin")
    }
}

sqldelight {
    database("Db") {
        packageName = "db"
        schemaOutputDirectory = file("src/main/sqldelight/$packageName/schemas")
        dialect = "sqlite:3.25"
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // Simplifies non-blocking programming
    // https://github.com/Kotlin/kotlinx.coroutines/releases
    val coroutinesVer = "1.7.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVer")

    // KTX extensions provide concise, idiomatic Kotlin to Jetpack, Android platform, and other APIs
    implementation("androidx.core:core-ktx:1.10.0")

    val fragmentVer = "1.4.1"
    implementation("androidx.fragment:fragment-ktx:$fragmentVer")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVer")

    // Simplifies in-app navigation, assumes single activity pattern
    val navVer = "2.5.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVer")
    implementation("androidx.navigation:navigation-ui-ktx:$navVer")

    // Background job scheduler
    // Used to fetch new data in background
    val workVer = "2.7.1"
    implementation("androidx.work:work-runtime-ktx:$workVer")
    androidTestImplementation("androidx.work:work-testing:$workVer")

    // In-app browser, it's about 2x faster than calling an external browser
    implementation("androidx.browser:browser:1.5.0")

    // Provides lifecycle-aware coroutine scopes
    val lifecycleVer = "2.5.1"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVer")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVer")

    // Helps to keep view hierarchies flat
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // List widget
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // Enables swipe to refresh pattern
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Retrofit turns HTTP APIs into Java interfaces
    // Used to communicate with remote backends
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Modern HTTP client
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:mockwebserver")

    // Bundle SQLite binaries
    implementation("com.github.requery:sqlite-android:3.39.2")

    // SQLDelight generates typesafe kotlin APIs from SQL statements
    val sqlDelightVer = "1.5.4"
    implementation("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVer")
    implementation("com.squareup.sqldelight:android-driver:$sqlDelightVer")
    testImplementation("com.squareup.sqldelight:sqlite-driver:$sqlDelightVer")

    // Dependency injection framework
    val koinAnnotationsVer = "1.0.0"
    implementation("io.insert-koin:koin-android:3.2.2")
    implementation("io.insert-koin:koin-annotations:$koinAnnotationsVer")
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVer")

    // Material design components
    implementation("com.google.android.material:material:1.8.0")

    // Used to download, cache and display images
    implementation("com.squareup.picasso:picasso:2.71828")

    // Java HTML parser
    // Used to auto-discover feed links
    implementation("org.jsoup:jsoup:1.14.3")

    // Feed parser
    // Used in standalone mode
    implementation("co.appreactor:feedk:0.2.6")

    // Custom global exception handler
    val acraVer = "5.9.5"
    implementation("ch.acra:acra-mail:$acraVer")
    implementation("ch.acra:acra-dialog:$acraVer")

    // Core test infrastructure
    testImplementation("junit:junit:4.13.2")

    // Mocking library, better to go easy on that
    testImplementation("io.mockk:mockk:1.12.4")

    // Core test infrastructure
    androidTestImplementation("junit:junit:4.13.2")

    // An instrumentation that runs various types of test cases
    androidTestImplementation("androidx.test:runner:1.5.2")

    // UI testing framework
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

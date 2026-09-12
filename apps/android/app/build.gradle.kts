import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use(::load)
}
val signingValues = mapOf(
    "storeFile" to "ANDROID_UPLOAD_KEYSTORE_PATH",
    "storePassword" to "ANDROID_UPLOAD_STORE_PASSWORD",
    "keyAlias" to "ANDROID_UPLOAD_KEY_ALIAS",
    "keyPassword" to "ANDROID_UPLOAD_KEY_PASSWORD",
).mapValues { (property, variable) ->
    providers.environmentVariable(variable).orNull ?: releaseProperties.getProperty(property)
}
val hasReleaseSigning = signingValues.values.any { !it.isNullOrEmpty() }
if (hasReleaseSigning) {
    require(signingValues.values.all { !it.isNullOrEmpty() }) {
        "Incomplete release signing. Set all four ANDROID_UPLOAD_* values or complete keystore.properties."
    }
}

android {
    namespace = "dev.hsichen.colorinvo"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.hsichen.colorinvo"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.gradleProperty("versionCode").orElse("1").get().toInt()
        versionName = providers.gradleProperty("versionName").orElse("0.1.0").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(signingValues.getValue("storeFile")!!)
                storePassword = signingValues.getValue("storePassword")
                keyAlias = signingValues.getValue("keyAlias")
                keyPassword = signingValues.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

    lint {
        warningsAsErrors = true
        // API 37 is not in the stable SDK channel yet; these versions are intentionally API 36-compatible.
        disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "ObsoleteSdkInt", "OldTargetApi")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.google.zxing:core:3.5.4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

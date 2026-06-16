plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

fun projectValue(name: String, defaultValue: String): String {
    return providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .getOrElse(defaultValue)
}

fun optionalProjectValue(name: String) = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))

fun buildConfigString(value: String): String {
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "com.elg.studly"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.elg.studly"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField(
            "String",
            "MYGES_API_BASE_URL",
            buildConfigString(projectValue("MYGES_API_BASE_URL", "https://api.kordis.fr/"))
        )
        buildConfigField(
            "String",
            "KORDIS_OAUTH_AUTHORIZE_URL",
            buildConfigString(projectValue("KORDIS_OAUTH_AUTHORIZE_URL", "https://authentication.kordis.fr/oauth/authorize?response_type=token&client_id=skolae-app"))
        )
        buildConfigField(
            "String",
            "KORDIS_OAUTH_REDIRECT_URI",
            buildConfigString(projectValue("KORDIS_OAUTH_REDIRECT_URI", "comreseaugesskolae:/oauth2redirect"))
        )
        buildConfigField(
            "String",
            "MYGES_USER_AGENT",
            buildConfigString(projectValue("MYGES_USER_AGENT", "MyGES Android"))
        )
    }

    signingConfigs {
        val releaseSigning = mapOf(
            "storeFile" to optionalProjectValue("MYGES_RELEASE_STORE_FILE").orNull,
            "storePassword" to optionalProjectValue("MYGES_RELEASE_STORE_PASSWORD").orNull,
            "keyAlias" to optionalProjectValue("MYGES_RELEASE_KEY_ALIAS").orNull,
            "keyPassword" to optionalProjectValue("MYGES_RELEASE_KEY_PASSWORD").orNull
        )
        if (releaseSigning.values.all { !it.isNullOrBlank() }) {
            create("release") {
                storeFile = file(releaseSigning.getValue("storeFile")!!)
                storePassword = releaseSigning.getValue("storePassword")
                keyAlias = releaseSigning.getValue("keyAlias")
                keyPassword = releaseSigning.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.play.app.update)
    implementation(libs.play.review)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

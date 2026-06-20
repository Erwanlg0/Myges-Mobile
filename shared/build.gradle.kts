plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    androidLibrary {
        namespace = "com.elg.studly.shared"
        compileSdk = 36
        minSdk = 26

        // Run common tests cheaply on the JVM (ubuntu CI) instead of only on a macOS runner.
        withHostTest {}
    }

    // ponytail: iosArm64 = real devices, iosSimulatorArm64 = Apple-Silicon simulator.
    // Skipped iosX64 (Intel-mac simulator) — add it only if you must run on Intel Macs.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

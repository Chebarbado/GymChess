plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

android {
    namespace = "com.rnr.gymchess"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.rnr.gymchess"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
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
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = false
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoExcludes = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*\$Companion.class",
    "**/*\$WhenMappings.class"
)

fun configureJacocoCoverageData(task: org.gradle.testing.jacoco.tasks.JacocoReportBase) {
    val buildDir = layout.buildDirectory.get().asFile
    task.classDirectories.setFrom(
        fileTree(buildDir) {
            include("**/classes/**/*.class")
            include("**/tmp/kotlin-classes/**/*.class")
            exclude(jacocoExcludes)
        }
    )
    task.sourceDirectories.setFrom(files("src/main/java"))
    task.executionData.setFrom(
        fileTree(buildDir) {
            include("**/*.exec")
        }
    )
}

val jacocoDebugUnitTestReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Generates JaCoCo coverage report for debug unit tests."

    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    configureJacocoCoverageData(this)
}

val verifyMinimumUnitTestCoverage by tasks.registering(JacocoCoverageVerification::class) {
    group = "verification"
    description = "Verifies that unit test coverage is at least 20%."

    dependsOn(jacocoDebugUnitTestReport)
    configureJacocoCoverageData(this)

    violationRules {
        rule {
            limit {
                minimum = 0.20.toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(verifyMinimumUnitTestCoverage)
}

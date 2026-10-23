import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val edgeRuntimeBaseUrl = providers.gradleProperty("edgeRuntimeBaseUrl")
    .orElse("https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1")
    .get()
val useRemoteDeals = providers.gradleProperty("useRemoteDeals").orNull == "true"

val signingFile = rootProject.file("keystore.properties")
val signingProperties = Properties().apply { if (signingFile.exists()) signingFile.inputStream().use { load(it) } }

android {
    signingConfigs {
        if (signingFile.exists()) create("release") {
            storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
            storeType = "PKCS12"
        }
    }
    namespace = "com.alejx.tiny_price"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.alejx.tiny_price"
        minSdk = 23
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        buildConfigField("String", "SUPABASE_URL", "\"https://ddzgaqjsoxsohkrsoowj.supabase.co\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_lqLtqNVmFj0CZUnGerM9SQ_UcCBfktV\"")
        // URL pública del proyecto; se puede sustituir mediante -PedgeRuntimeBaseUrl.
        buildConfigField("String", "EDGE_RUNTIME_BASE_URL", "\"$edgeRuntimeBaseUrl\"")
        // Keep preview data by default. Enable the hosted function with:
        // -PuseRemoteDeals=true
        buildConfigField("boolean", "USE_REMOTE_DEALS", useRemoteDeals.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            buildConfigField("boolean", "USE_REMOTE_DEALS", "true")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

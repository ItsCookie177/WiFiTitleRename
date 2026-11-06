import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val appDisplayName = "HyperMint"
val appVersionName = "1.7.0"

android {
    namespace = "com.wifirename.module"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.wifirename.module"
        minSdk = 31
        targetSdk = 35
        versionCode = 12
        versionName = appVersionName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.4")
    implementation("androidx.activity:activity-compose:1.13.0")
    compileOnly(files("libs/api-82.jar"))
}

listOf("debug", "release").forEach { variantName ->
    val taskSuffix = variantName.replaceFirstChar { it.uppercaseChar() }
    val copyApk = tasks.register("copy${taskSuffix}Apk") {
        doLast {
            val dir = layout.buildDirectory.dir("outputs/apk/$variantName").get().asFile
            val target = File(dir, "$appDisplayName-v$appVersionName.apk")
            val source = dir.listFiles { file ->
                file.isFile && file.extension == "apk" && file.name != target.name
            }?.maxByOrNull { it.lastModified() }
            source?.copyTo(target, overwrite = true)
        }
    }
    tasks.matching { it.name == "assemble$taskSuffix" }.configureEach {
        finalizedBy(copyApk)
    }
}

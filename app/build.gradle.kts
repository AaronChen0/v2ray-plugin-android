import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.application")
    kotlin("android")
}

val flavorRegex = "(assemble|generate)\\w*(Release|Debug)".toRegex()
val currentFlavor get() = gradle.startParameter.taskRequests.toString().let { task ->
                      flavorRegex.find(task)?.groupValues?.get(2)?.lowercase() ?: "debug".also {
                                                               println("Warning: No match found for $task")
                                                           }
                  }

val minSdk1 = 30
val targetSdk1 = 34

android {
    namespace = "com.github.shadowsocks.plugin.v2ray"
    val javaVersion = JavaVersion.VERSION_17
    ndkVersion = "27.0.11902837"
    compileSdk = targetSdk1
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    defaultConfig {
        applicationId = "com.github.shadowsocks.plugin.v2ray"
        minSdk = minSdk1
        targetSdk = targetSdk1
        versionCode = 1030301
        versionName = "1.3.4"
    }
    


    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
        }
    }
    sourceSets.getByName("main") {
        jniLibs.srcDirs("$projectDir/build/go")
    }

    
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    if (currentFlavor == "release")
        applicationVariants.all {
            val variant = this
            val versionCodes = mapOf("arm64-v8a" to 2)

            variant.outputs
                .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
                .forEach { output ->
                    val abi = if (output.getFilter("ABI") != null)
                        output.getFilter("ABI")
                    else
                        "universal"

                    if(versionCodes.containsKey(abi))
                    {
                        output.versionCodeOverride = (versionCodes[abi]!!).plus(variant.versionCode)
                    }
                    else
                    {
                        return@forEach
                    }
                }

        }
}

tasks.register<Exec>("goBuild") {
    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        println("Warning: Building on Windows is not supported")
    } else {
        executable("/bin/bash")
        args("go-build.bash", minSdk1)
        environment("ANDROID_HOME", android.sdkDirectory)
        environment("ANDROID_NDK_HOME", android.ndkDirectory)
    }
}

tasks.whenTaskAdded {
    when (name) {
        "mergeDebugJniLibFolders", "mergeReleaseJniLibFolders" -> dependsOn("goBuild")
    }
}

dependencies {
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.github.shadowsocks:plugin:2.0.1")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
}

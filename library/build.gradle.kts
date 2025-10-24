plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.agcp)
    id("maven-publish")
}

android {
    namespace = "com.gooogle.analysis.library"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.agconnect.remoteconfig)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //implementation(project(":lib-memory"))
    //implementation(project(":lib-cpu"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.gooogle.analysis"
                artifactId = "library"
                version = "1.0.0"

                artifact(tasks.getByName("bundleReleaseAar"))
            }
        }
    }
}

// 禁用所有生成 sources.jar 的任务
tasks.withType<org.gradle.jvm.tasks.Jar> {
    // 检查任务的分类器（classifier）。sourcesJar 任务通常将其设置为 'sources'。
    if (archiveClassifier.getOrNull() == "sources") {
        // 将任务设置为禁用状态
        enabled = false
        // 打印信息，确认任务被禁用，有助于调试
        println("Disabling sourcesJar task: $name")
    }
}

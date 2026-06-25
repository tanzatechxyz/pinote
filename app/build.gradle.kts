plugins {
    id("com.android.application")
}

android {
    namespace = "com.topenclaw.pinote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.topenclaw.pinote"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
}

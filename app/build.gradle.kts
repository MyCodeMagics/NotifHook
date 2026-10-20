plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "com.inibir.notifhook"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.inibir.notifhook"
		minSdk = 26
		targetSdk = 34
		versionCode = 4
		versionName = "1.3"
	}

	val keystorePath: String? = System.getenv("CM_KEYSTORE_PATH")

	signingConfigs {
		if (keystorePath != null) {
			create("fixed") {
				storeFile = file(keystorePath)
				storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
				keyAlias = System.getenv("CM_KEY_ALIAS")
				keyPassword = System.getenv("CM_KEY_PASSWORD")
			}
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			signingConfig = signingConfigs.getByName(if (keystorePath != null) "fixed" else "debug")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}
}

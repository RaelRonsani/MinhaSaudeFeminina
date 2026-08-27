# ============================================================================
# ADIÇÕES DE BUILD PARA SQLDELIGHT (banco local multiplataforma)
# ============================================================================

# ------------------------------------------------------------
# 1. gradle/libs.versions.toml
# ------------------------------------------------------------
# Em [versions], ADICIONE:

sqldelight = "2.0.2"

# Em [libraries], ADICIONE:

sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }

# Em [plugins], ADICIONE:

sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }


# ------------------------------------------------------------
# 2. build.gradle.kts DA RAIZ (Project)
# ------------------------------------------------------------
# No bloco plugins, ADICIONE a última linha:

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false      // <-- ADICIONAR
}


# ------------------------------------------------------------
# 3. composeApp/build.gradle.kts
# ------------------------------------------------------------
# No bloco plugins do módulo, ADICIONE:

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)                  // <-- ADICIONAR
}

# Em androidMain.dependencies, ADICIONE:
    implementation(libs.sqldelight.android.driver)

# Em iosMain.dependencies, ADICIONE:
    implementation(libs.sqldelight.native.driver)

# Em commonMain.dependencies, ADICIONE:
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)

# ABAIXO do bloco android { ... }, ADICIONE (fora do android, na raiz do arquivo):

sqldelight {
    databases {
        create("BemVindaDb") {
            packageName.set("com.bemvinda.app.db")
        }
    }
}

// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // FIX (баг 9): KSP подключается через alias из libs.versions.toml.
    // Ранее был хардкод: id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    // — версия не соответствовала Kotlin в toml и не использовала version catalog.
    alias(libs.plugins.ksp) apply false
}
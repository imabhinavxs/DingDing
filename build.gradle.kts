// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Version Catalog aliases (from libs.versions.toml)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Manual plugin declarations (only if not in version catalog)
    id("com.google.gms.google-services") version "4.3.15" apply false  // Firebase
}

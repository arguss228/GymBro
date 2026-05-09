# Obsession ProGuard rules
# Room
-keep class com.obsession.app.data.local.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp

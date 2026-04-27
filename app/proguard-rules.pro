# GymBro ProGuard rules
# Room
-keep class com.gymbro.app.data.local.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp

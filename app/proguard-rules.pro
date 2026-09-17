# E-Money Checker ProGuard / R8 Rules

# Preserve Domain Models
-keep class com.emoneychecker.domain.model.** { *; }

# Preserve APDU / IsoDep Wrappers
-keep class com.emoneychecker.nfc.apdu.** { *; }
-keep class com.emoneychecker.nfc.parser.** { *; }

# Preserve ViewModel classes for reflection/DI
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Preserve Compose runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

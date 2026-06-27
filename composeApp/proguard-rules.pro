# ──────────────────────────────────────────────────────────────────────────────
# Klarity — R8 / ProGuard keep rules for the release build.
#
# Strategy: keep ALL app code (it's small, and our @Serializable models, SQLDelight
# row types, and Koin-resolved types are touched reflectively/generically). R8 still
# shrinks + optimizes the large dependencies (Compose, Ktor, kotlinx, …), which is
# where the size win actually comes from. Once a release build is verified on-device,
# this app-wide keep can be tightened.
# ──────────────────────────────────────────────────────────────────────────────

# --- App code ---
-keep class com.example.klarity.** { *; }
-keepclassmembers class com.example.klarity.** { *; }

# --- kotlinx.serialization (the artifact also ships consumer rules; this is belt-and-braces) ---
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, EnclosingMethod
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }

# --- Coroutines ---
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- Ktor / OkHttp / SLF4J (Ktor ships its own rules; just silence optional deps) ---
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**

# --- SQLDelight ---
-dontwarn app.cash.sqldelight.**

# --- Koin ---
-dontwarn org.koin.**

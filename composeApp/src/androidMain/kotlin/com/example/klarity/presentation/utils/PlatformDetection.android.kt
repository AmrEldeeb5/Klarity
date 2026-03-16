package com.example.klarity.presentation.utils

import android.os.Build

/**
 * Android Platform Detection Implementation
 */

actual fun isMacOS(): Boolean {
    // Android is never macOS
    return false
}

actual fun isWindows(): Boolean {
    // Android is never Windows
    return false
}

actual fun isLinux(): Boolean {
    // Android is based on Linux, but for keyboard shortcuts
    // we treat it differently
    return false
}

actual fun platformName(): String {
    return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}

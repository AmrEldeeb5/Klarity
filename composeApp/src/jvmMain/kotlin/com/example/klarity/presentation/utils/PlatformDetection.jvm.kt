package com.example.klarity.presentation.utils

/**
 * JVM (Desktop) Platform Detection Implementation
 */

actual fun isMacOS(): Boolean {
    val osName = System.getProperty("os.name").lowercase()
    return osName.contains("mac") || osName.contains("darwin")
}

actual fun isWindows(): Boolean {
    val osName = System.getProperty("os.name").lowercase()
    return osName.contains("win")
}

actual fun isLinux(): Boolean {
    val osName = System.getProperty("os.name").lowercase()
    return osName.contains("nux") || osName.contains("nix") || osName.contains("aix")
}

actual fun platformName(): String {
    return System.getProperty("os.name")
}

package com.example.klarity.presentation.utils

/**
 * Platform Detection Utilities
 * 
 * Provides platform-specific detection for keyboard shortcuts
 * and UI adjustments.
 */

/**
 * Returns true if the current platform is macOS.
 * Used to display platform-specific keyboard shortcuts (⌘ vs Ctrl).
 */
expect fun isMacOS(): Boolean

/**
 * Returns true if the current platform is Windows.
 */
expect fun isWindows(): Boolean

/**
 * Returns true if the current platform is Linux.
 */
expect fun isLinux(): Boolean

/**
 * Returns the name of the current platform.
 */
expect fun platformName(): String

package com.example.klarity.presentation.screen.focus

/**
 * Sound player for focus mode audio feedback.
 * 
 * Platform-specific implementations should provide actual sound playback.
 * This is an abstract interface for cross-platform compatibility.
 */
interface FocusSoundPlayer {
    /**
     * Play sound when a work session is completed.
     */
    fun playSessionComplete()
    
    /**
     * Play sound when a break is completed.
     */
    fun playBreakComplete()
    
    /**
     * Play a tick sound every second (optional feature).
     */
    fun playTick()
    
    /**
     * Enable or disable all sounds.
     */
    fun setSoundEnabled(enabled: Boolean)
}

/**
 * Default implementation that does nothing.
 * Platform-specific modules should provide actual implementations.
 */
class NoOpFocusSoundPlayer : FocusSoundPlayer {
    override fun playSessionComplete() {
        // No-op
    }
    
    override fun playBreakComplete() {
        // No-op
    }
    
    override fun playTick() {
        // No-op
    }
    
    override fun setSoundEnabled(enabled: Boolean) {
        // No-op
    }
}

/**
 * Desktop notification helper for focus mode.
 * 
 * Platform-specific implementations should provide actual notifications.
 */
interface FocusNotifications {
    /**
     * Show a notification with title and message.
     */
    fun showNotification(title: String, message: String)
}

/**
 * Default no-op implementation.
 */
class NoOpFocusNotifications : FocusNotifications {
    override fun showNotification(title: String, message: String) {
        // No-op
    }
}

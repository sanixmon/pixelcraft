package com.app.pixelcraft

import com.app.pixelcraft.data.update.AppUpdater
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdaterVersionTest {
    @Test
    fun newerVersionsAreDetected() {
        assertTrue(AppUpdater.isNewer("0.3.2", "0.3.1"))
        assertTrue(AppUpdater.isNewer("0.10.0", "0.9.9"))
        assertTrue(AppUpdater.isNewer("1.0", "0.99.99"))
    }

    @Test
    fun sameOrOlderVersionsAreNotNewer() {
        assertFalse(AppUpdater.isNewer("0.3.1", "0.3.1"))
        assertFalse(AppUpdater.isNewer("0.3.0", "0.3.1"))
        assertFalse(AppUpdater.isNewer("0.3", "0.3.0"))
    }
}

package com.app.pixelcraft

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.StrictMode
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.GallerySaver
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.TextGeometryCache
import com.app.pixelcraft.domain.UserPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Lightweight manual Dependency Injection container (§ 3.6) providing singletons across the app
 * while remaining trivially replaceable in JVM unit tests without Hilt/KSP overhead.
 */
class AppContainer(context: Application) {
    val projectStorage: ProjectStorage by lazy { ProjectStorage(context) }
    val draftStore: DraftStore by lazy { DraftStore(context) }
    val imageStore: ImageStore by lazy { ImageStore(context) }
    val imageExporter: ImageExporter by lazy { ImageExporter(context) }
    val gallerySaver: GallerySaver by lazy { GallerySaver(context) }
    val preferencesStore: UserPreferencesStore by lazy { UserPreferencesStore(context) }
}

class PixelCraftApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        // Background startup tasks: preload starter fonts (§ 4.7) & garbage-collect orphan images >24h (Bug 2.4)
        appScope.launch {
            runCatching {
                FontCatalog.preloadStarterFonts(this@PixelCraftApp)
            }
            runCatching {
                val projectRefs = container.projectStorage.collectAllReferencedImageNames()
                val draftRefs = buildSet {
                    val draft = container.draftStore.load()
                    draft?.snapshot?.canvasConfig?.customImageUri?.takeIf { it.isNotBlank() }?.let(::add)
                    draft?.snapshot?.layers?.forEach { layer ->
                        layer.imageLocalPath?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
                container.imageStore.collectOrphanImages(projectRefs + draftRefs)
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (::container.isInitialized) {
            container.imageStore.trimMemory(level)
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ||
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
        ) {
            TextGeometryCache.evictAll()
        }
    }
}

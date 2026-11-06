package com.app.pixelcraft

import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.DraftPayload
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.editor.EditorHistory
import com.app.pixelcraft.domain.editor.LayerOperations
import com.app.pixelcraft.domain.editor.LayerOperations.bringToFront
import com.app.pixelcraft.domain.editor.LayerOperations.deleteUnlocked
import com.app.pixelcraft.domain.editor.LayerOperations.duplicate
import com.app.pixelcraft.domain.editor.LayerOperations.groupLayers
import com.app.pixelcraft.domain.editor.LayerOperations.normalizeZIndices
import com.app.pixelcraft.domain.editor.LayerOperations.sendToBack
import com.app.pixelcraft.domain.editor.LayerOperations.ungroupLayers
import com.app.pixelcraft.presentation.AlignmentAction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EditorArchitectureTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun editorHistoryCoalescesRapidNudgesWithinWindowAndSupportsJumpToIndex() {
        var fakeClockMs = 1_000L
        val history = EditorHistory<Int>(
            limit = 5,
            coalesceWindowMs = 500L,
            clock = { fakeClockMs }
        )

        // Rapid nudges 100ms apart with same coalesceTag coalesce into ONE undo entry
        history.beginStep(0, label = "Nudge", continuous = false, coalesceTag = "nudge_1")
        fakeClockMs += 120L
        history.beginStep(1, label = "Nudge", continuous = false, coalesceTag = "nudge_1")
        fakeClockMs += 120L
        history.beginStep(2, label = "Nudge", continuous = false, coalesceTag = "nudge_1")
        assertEquals(1, history.undoSize)

        // Pause > 500ms starts a new undo entry
        fakeClockMs += 650L
        history.beginStep(3, label = "Nudge", continuous = false, coalesceTag = "nudge_1")
        assertEquals(2, history.undoSize)

        fakeClockMs += 700L
        history.beginStep(4, label = "Recolor", continuous = false)
        assertEquals(3, history.undoSize)
        assertEquals(listOf("Nudge", "Nudge", "Recolor"), history.undoEntries().map { it.label })

        // Jump straight back to index 0 (initial state = 0), pushing intermediate states to redo!
        val restored = history.jumpToUndoIndex(0, currentSnapshot = 5)
        assertEquals(0, restored)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)

        // Redo all the way forward returns 3 -> 4 -> 5
        assertEquals(3, history.redo(0))
        assertEquals(4, history.redo(3))
        assertEquals(5, history.redo(4))
    }

    @Test
    fun pureLayerOperationsMaintainContiguousZOrderLockingAndGrouping() {
        val l1 = Layer(id = "a", name = "Bottom", type = LayerType.TEXT, zIndex = 10)
        val l2 = Layer(id = "b", name = "Middle", type = LayerType.SHAPE, zIndex = 20, isLocked = true)
        val l3 = Layer(id = "c", name = "Top", type = LayerType.IMAGE, zIndex = 30)

        val normalized = listOf(l3, l1, l2).normalizeZIndices()
        assertEquals(listOf("a" to 0, "b" to 1, "c" to 2), normalized.sortedBy { it.zIndex }.map { it.id to it.zIndex })

        val brought = normalized.bringToFront("a")
        assertEquals(listOf("b", "c", "a"), brought.sortedBy { it.zIndex }.map { it.id })

        val sentBack = brought.sendToBack("a")
        assertEquals(listOf("a", "b", "c"), sentBack.sortedBy { it.zIndex }.map { it.id })

        // Locked layer cannot be deleted via deleteUnlocked
        assertEquals(3, sentBack.deleteUnlocked("b").size)
        assertEquals(2, sentBack.deleteUnlocked("a").size)

        val (withDup, dupLayer) = sentBack.duplicate("c")!!
        assertEquals(4, withDup.size)
        assertEquals("Top Copy", dupLayer.name)

        val grouped = withDup.groupLayers(setOf("a", "c"))
        val gid = grouped.first { it.id == "a" }.groupId
        assertNotNull(gid)
        assertEquals(gid, grouped.first { it.id == "c" }.groupId)

        val ungrouped = grouped.ungroupLayers(setOf("a"))
        assertNull(ungrouped.first { it.id == "a" }.groupId)
        assertNull(ungrouped.first { it.id == "c" }.groupId)

        val aligned = LayerOperations.alignSingleLayer(
            l1.copy(width = 200f, height = 100f),
            canvasWidth = 1000f,
            canvasHeight = 800f,
            action = AlignmentAction.ALIGN_LEFT
        )
        assertEquals(-400f, aligned.positionX, 0.01f)
    }

    @Test
    fun draftStoreAndImageStoreNormalizeRelativePathsAndGarbageCollectOrphans() = runTest {
        val draftFile = File(tempFolder.newFolder(), "draft.json")
        val draftStore = DraftStore { draftFile }

        val legacySnapshot = EditorSnapshot(
            layers = listOf(
                Layer(
                    id = "img1",
                    name = "Photo",
                    type = LayerType.IMAGE,
                    imageLocalPath = "/data/user/0/com.app.pixelcraft/files/images/hero_pic.jpg"
                )
            ),
            canvasConfig = CanvasConfig(
                backgroundType = BackgroundType.CUSTOM_IMAGE,
                customImageUri = "/storage/emulated/0/old/bg_pic.png"
            ),
            selectedLayerId = "img1"
        )

        draftStore.save(
            DraftPayload(
                timestamp = 55_000L,
                currentProjectId = null,
                currentProjectName = "Crash Safe Draft",
                isDirty = true,
                snapshot = legacySnapshot
            )
        ).getOrThrow()

        assertTrue(draftStore.hasDraftSync())
        val loadedDraft = draftStore.load()
        assertNotNull(loadedDraft)
        assertEquals("hero_pic.jpg", loadedDraft!!.snapshot.layers.single().imageLocalPath)
        assertEquals("bg_pic.png", loadedDraft.snapshot.canvasConfig.customImageUri)

        draftStore.clear()
        assertFalse(draftStore.hasDraftSync())

        // Verify ImageStore 24-hour orphan garbage collection (Bug 2.4 & Bug 2.5)
        val imagesDir = tempFolder.newFolder("images")
        val imageStore = ImageStore({ imagesDir }, context = null)

        val now = 1_700_000_000_000L
        val twoDaysAgo = now - (48L * 60L * 60L * 1000L)
        val tenMinutesAgo = now - (10L * 60L * 1000L)

        val referencedOld = File(imagesDir, "hero_pic.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3))
            setLastModified(twoDaysAgo)
        }
        val orphanRecent = File(imagesDir, "recent_undo.jpg").apply {
            writeBytes(byteArrayOf(4, 5, 6))
            setLastModified(tenMinutesAgo)
        }
        val orphanStale = File(imagesDir, "stale_orphan.jpg").apply {
            writeBytes(byteArrayOf(7, 8, 9))
            setLastModified(twoDaysAgo)
        }

        val deleted = imageStore.collectOrphanImages(
            referencedPaths = setOf("/legacy/path/hero_pic.jpg"),
            nowMs = now
        )
        assertEquals(1, deleted)
        assertTrue("Referenced image must never be deleted", referencedOld.exists())
        assertTrue("Recent (<24h) unreferenced image must stay for undo safety", orphanRecent.exists())
        assertFalse("Stale (>24h) orphan image must be garbage collected", orphanStale.exists())
    }

    @Test
    fun projectStorageMigratesPathsExportsAndImportsPixelcraftArchivesAndBlocksZipSlip() = runTest {
        val srcProjectsDir = tempFolder.newFolder("src_projects")
        val srcImagesDir = tempFolder.newFolder("src_images")
        val dstProjectsDir = tempFolder.newFolder("dst_projects")
        val dstImagesDir = tempFolder.newFolder("dst_images")

        val imageBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        File(srcImagesDir, "layer_asset.png").writeBytes(imageBytes)

        val srcStorage = ProjectStorage { srcProjectsDir }
        val dstStorage = ProjectStorage { dstProjectsDir }

        val saved = srcStorage.saveProject(
            ProjectData(
                id = "p_arch",
                name = "Portable Poster",
                timestamp = 999_000L,
                schemaVersion = 1,
                snapshot = EditorSnapshot(
                    layers = listOf(
                        Layer(
                            id = "l_img",
                            name = "Asset",
                            type = LayerType.IMAGE,
                            imageLocalPath = "/data/user/0/com.app.pixelcraft/files/images/layer_asset.png"
                        )
                    ),
                    canvasConfig = CanvasConfig(width = 1080, height = 1350),
                    selectedLayerId = "l_img"
                )
            )
        ).getOrThrow()

        // Relative filename migration verified
        assertEquals("layer_asset.png", saved.snapshot.layers.single().imageLocalPath)
        assertEquals(1, srcStorage.listProjectSummaries().getOrThrow().size)
        assertEquals(setOf("layer_asset.png"), srcStorage.collectAllReferencedImageNames())

        // Export to .pixelcraft ZIP archive in memory
        val archiveOut = ByteArrayOutputStream()
        srcStorage.exportProjectArchive("p_arch", srcImagesDir, archiveOut).getOrThrow()

        // Import into clean destination storage & images directory
        val imported = dstStorage.importProjectArchive(
            ByteArrayInputStream(archiveOut.toByteArray()),
            dstImagesDir
        ).getOrThrow()

        assertEquals("Portable Poster", imported.name)
        val importedImgName = imported.snapshot.layers.single().imageLocalPath!!
        val copiedImgFile = File(dstImagesDir, importedImgName)
        assertTrue("Archive import must unpack referenced image into destination imagesDir", copiedImgFile.exists())
        assertArrayEquals(imageBytes, copiedImgFile.readBytes())

        // Security check: crafted ZIP with "../evil.txt" Zip-Slip entry must be rejected
        val maliciousZip = ByteArrayOutputStream().also { baos ->
            ZipOutputStream(baos).use { zos ->
                zos.putNextEntry(ZipEntry("images/../../evil.txt"))
                zos.write("pwn".toByteArray())
                zos.closeEntry()
            }
        }
        val maliciousResult = dstStorage.importProjectArchive(
            ByteArrayInputStream(maliciousZip.toByteArray()),
            dstImagesDir
        )
        assertTrue("Zip-Slip traversal entry must be rejected", maliciousResult.isFailure)
    }
}

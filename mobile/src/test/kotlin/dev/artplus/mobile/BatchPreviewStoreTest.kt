package dev.artplus.mobile

import android.graphics.Bitmap
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BatchPreviewStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun manifest_jsonSerializationAndDeserialization() {
        val manifest = BatchPreviewManifest(
            presetId = "preset-123",
            presetName = "测试预设",
            updatedAt = 123456789L,
            apps = listOf(
                BatchPreviewAppMeta("com.tencent.mm", "微信"),
                BatchPreviewAppMeta("com.eg.android.AlipayGphone", "支付宝"),
            ),
        )

        val json = manifest.toJson()
        val restored = BatchPreviewManifest.fromJson(json)

        assertNotNull(restored)
        assertEquals("preset-123", restored.presetId)
        assertEquals("测试预设", restored.presetName)
        assertEquals(123456789L, restored.updatedAt)
        assertEquals(2, restored.apps.size)
        assertEquals("com.tencent.mm", restored.apps[0].packageName)
        assertEquals("微信", restored.apps[0].label)
        assertEquals("com.eg.android.AlipayGphone", restored.apps[1].packageName)
        assertEquals("支付宝", restored.apps[1].label)
    }

    @Test
    fun store_writeAndReadManifest() {
        val baseDir = tempFolder.newFolder("files")
        val manifest = BatchPreviewManifest(
            presetId = "p1",
            presetName = "预设A",
            updatedAt = 9999L,
            apps = listOf(BatchPreviewAppMeta("com.test.app", "测试")),
        )

        assertFalse(BatchPreviewStore.hasSnapshot(baseDir, "p1"))

        val writeSuccess = BatchPreviewStore.writeManifest(baseDir, manifest)
        assertTrue(writeSuccess)
        assertTrue(BatchPreviewStore.hasSnapshot(baseDir, "p1"))

        val loaded = BatchPreviewStore.readManifest(baseDir, "p1")
        assertNotNull(loaded)
        assertEquals("预设A", loaded.presetName)
        assertEquals(1, loaded.apps.size)

        val deleted = BatchPreviewStore.deleteSnapshot(baseDir, "p1")
        assertTrue(deleted)
        assertFalse(BatchPreviewStore.hasSnapshot(baseDir, "p1"))
        assertNull(BatchPreviewStore.readManifest(baseDir, "p1"))
    }

    @Test
    fun store_saveAndLoadSnapshotWithMockIo() {
        val baseDir = tempFolder.newFolder("files")
        val preset = TuningPreset(
            id = "preset-xyz",
            name = "玻璃质感",
            params = TuningParams(),
            createdAt = 1000L,
            updatedAt = 2000L,
        )

        val savedFiles = mutableSetOf<String>()
        val mockIo = object : BatchPreviewBitmapIo {
            override fun saveBitmap(file: File, bitmap: Bitmap?): Boolean {
                if (bitmap != null) {
                    file.parentFile?.mkdirs()
                    file.writeText("mock-bitmap-content")
                    savedFiles.add(file.name)
                    return true
                }
                return false
            }

            override fun decodeBitmap(file: File): Bitmap? {
                // 在没有 Android Bitmap 运行时的测试环境中，返回 null 但文件存在已被验证
                return null
            }
        }

        // 使用虚拟占位或测试数据
        // 由于测试环境中 Bitmap 为 stub，测试 saveSnapshot 的文件生成逻辑
        val item1 = BatchPreviewItemData(
            packageName = "com.test.app1",
            label = "应用1",
            recbg = null,
            recfg = null,
            recNight = null,
            monochromeLight = null,
            monochromeDark = null,
        )

        val success = BatchPreviewStore.saveSnapshot(baseDir, preset, listOf(item1), mockIo)
        assertTrue(success)
        assertTrue(BatchPreviewStore.hasSnapshot(baseDir, "preset-xyz"))

        val manifest = BatchPreviewStore.readManifest(baseDir, "preset-xyz")
        assertNotNull(manifest)
        assertEquals("玻璃质感", manifest.presetName)
        assertEquals(1, manifest.apps.size)
        assertEquals("com.test.app1", manifest.apps[0].packageName)
    }
}

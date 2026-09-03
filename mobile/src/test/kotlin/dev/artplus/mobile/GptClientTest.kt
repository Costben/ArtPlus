package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.json.JSONArray
import org.json.JSONObject

/**
 * P4 管线守卫：GPT URL 归一化/SSE 解析/远端 URL 校验/提示词组装必须与原 Activity 内联逻辑一致。
 * 全部 JVM 直跑（org.json 真实现；不碰网络/Bitmap）。
 */
class GptClientTest {

    @Test
    fun normalizeResponsesUrl_variants() {
        assertEquals("https://x/v1/responses", normalizeResponsesUrl("https://x"))
        assertEquals("https://x/v1/responses", normalizeResponsesUrl("https://x/"))
        assertEquals("https://x/v1/responses", normalizeResponsesUrl("https://x/v1"))
        assertEquals("https://x/v1/responses", normalizeResponsesUrl("https://x/v1/responses"))
        assertEquals("https://x/openai/v1/responses", normalizeResponsesUrl("https://x/openai/v1"))
    }

    @Test
    fun normalizeImagesEditUrl_variants() {
        assertEquals("https://x/v1/images/edits", normalizeImagesEditUrl("https://x"))
        assertEquals("https://x/v1/images/edits", normalizeImagesEditUrl("https://x/v1"))
        assertEquals("https://x/v1/images/edits", normalizeImagesEditUrl("https://x/v1/images/edits"))
        assertEquals("https://x/openai/v1/images/edits", normalizeImagesEditUrl("https://x/openai/v1"))
    }

    @Test
    fun parseResponsesStream_collectsOutputAndPartialImage() {
        val text = "event: response.output_item.done\n" +
            "data: {\"type\":\"response.output_item.done\",\"item\":{\"type\":\"image_generation_call\",\"id\":\"1\"}}\n" +
            "\n" +
            "event: partial\n" +
            "data: {\"type\":\"response.image_generation_call.partial_image\",\"partial_image_b64\":\"QUJD\"}\n" +
            "\n" +
            "data: [DONE]\n"
        val parsed = parseResponsesStream(text)
        val output = parsed.getJSONArray("output")
        assertEquals(2, output.length())
        assertEquals("image_generation_call", output.getJSONObject(0).getString("type"))
        assertEquals("QUJD", output.getJSONObject(1).getString("image_base64"))
    }

    @Test
    fun parseResponsesStream_emptyYieldsEmptyOutput() {
        val parsed = parseResponsesStream("data: [DONE]\n")
        assertEquals(0, parsed.getJSONArray("output").length())
    }

    @Test
    fun extractImageBytes_noImageData_throws() {
        assertFailsWith<IllegalStateException> {
            extractImageBytes(JSONObject().put("output", JSONArray()), "k", true)
        }
    }

    @Test
    fun findImageBytes_emptyOrShort_returnsNull() {
        assertNull(findImageBytes(JSONArray(), "k", true))
        val items = JSONArray().put(JSONObject().put("b64", "short"))
        assertNull(findImageBytes(items, "k", true))
    }

    @Test
    fun looksLikeSvg_detects() {
        assertTrue(looksLikeSvg("<svg xmlns='x'/>".toByteArray()))
        assertTrue(looksLikeSvg("  <?xml version='1.0'?><svg>".toByteArray()))
        assertFalse(looksLikeSvg(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)))
    }

    @Test
    fun validatedRemoteUrl_rules() {
        assertEquals("https", validatedRemoteUrl("https://a/b", "AI", true).protocol)
        assertEquals("http", validatedRemoteUrl("http://a/b", "AI", true).protocol)
        assertFailsWith<IllegalStateException> {
            validatedRemoteUrl("http://a/b", "AI", false)
        }
        assertFailsWith<IllegalStateException> {
            validatedRemoteUrl("ftp://a/b", "AI", true)
        }
    }

    @Test
    fun activeGptForegroundPrompt_customAndDefault() {
        val custom = activeGptForegroundPrompt(" 画一只猫 ", GptPromptPreset.Custom, 70)
        assertTrue(custom.startsWith("画一只猫."))
        assertTrue(custom.endsWith("about 70% of the final square canvas."))
        val def = activeGptForegroundPrompt("", GptPromptPreset.Default, 100)
        assertTrue(def.startsWith("Keep only the app icon main subject/logo."))
        assertTrue(def.endsWith("about 100% of the final square canvas."))
        val blankCustomFallsBack = activeGptForegroundPrompt("  ", GptPromptPreset.Custom, 100)
        assertTrue(blankCustomFallsBack.startsWith("Extract only the visible foreground"))
    }

    @Test
    fun buildBackgroundPrompt_fixed() {
        assertEquals(
            "Remove the app icon main subject/logo. Rebuild only the clean original background plate. No logo, no text, no symbol.",
            buildBackgroundPrompt(),
        )
    }
}

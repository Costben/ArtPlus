package dev.artplus.mobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.json.JSONObject

/**
 * P4 system 守卫：Debug 服务器纯 helper（query 解析/头切分/JSON 扁平化）
 * 必须与原 Activity 内联逻辑一致。全部 JVM 直跑。
 */
class DebugServerTest {

    @Test
    fun parseQuery_pairsAndDecoding() {
        assertTrue(parseQuery("").isEmpty())
        assertTrue(parseQuery("  ").isEmpty())
        assertEquals(
            mapOf("a" to "1", "b" to "x y", "c" to ""),
            parseQuery("a=1&b=x%20y&c="),
        )
    }

    @Test
    fun urlDecode_utf8() {
        assertEquals("a b+c", urlDecode("a%20b%2Bc"))
    }

    @Test
    fun headerLines_splitsAndDropsBlanks() {
        val lines = headerLines("GET /debug/params HTTP/1.1\r\nHost: x\r\n\r\n")
        assertEquals(listOf("GET /debug/params HTTP/1.1", "Host: x"), lines)
    }

    @Test
    fun jsonToParamMap_skipsNulls() {
        val json = JSONObject()
            .put("a", "1")
            .put("b", JSONObject.NULL)
            .put("c", 42)
        assertEquals(mapOf("a" to "1", "c" to "42"), jsonToParamMap(json))
    }
}

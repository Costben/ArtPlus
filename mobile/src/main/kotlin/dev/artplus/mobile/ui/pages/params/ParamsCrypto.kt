package dev.artplus.mobile

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Slice 2.4：加解密/密钥纯移动（原 MainActivity 本体原样搬迁）。
 * 只做纯移动：算法、key 名与存储顺序一律不变。
 * 状态经显式参数注入（prefs）；MainActivity 留同名薄 wrapper（重构期间保留），调用点零改动。
 */

internal fun paramsLoadGptApiKey(prefs: SharedPreferences): String {
    val encrypted = prefs.getString(PREF_GPT_API_KEY_ENCRYPTED, null)
    val decrypted = encrypted
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { paramsDecryptSecret(it) }.getOrNull() }
    if (decrypted != null) {
        if (prefs.contains(PREF_GPT_API_KEY)) {
            prefs.edit().remove(PREF_GPT_API_KEY).apply()
        }
        return decrypted
    }
    val legacyPlain = prefs.getString(PREF_GPT_API_KEY, "") ?: ""
    if (legacyPlain.isNotBlank()) {
        val migrated = paramsEncryptSecret(legacyPlain)
        prefs.edit()
            .remove(PREF_GPT_API_KEY)
            .putString(PREF_GPT_API_KEY_ENCRYPTED, migrated)
            .apply()
    }
    return legacyPlain
}

internal fun paramsEncryptSecret(value: String): String {
    if (value.isBlank()) {
        return ""
    }
    val cipher = Cipher.getInstance(KEYSTORE_CIPHER_TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, paramsGptSecretKey())
    val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
    return listOf(cipher.iv, encrypted)
        .joinToString(":") { Base64.encodeToString(it, Base64.NO_WRAP) }
}

internal fun paramsDecryptSecret(value: String): String {
    val parts = value.split(':')
    if (parts.size != 2) {
        error("invalid encrypted secret")
    }
    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
    val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
    val cipher = Cipher.getInstance(KEYSTORE_CIPHER_TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, paramsGptSecretKey(), GCMParameterSpec(KEYSTORE_GCM_TAG_BITS, iv))
    return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
}

internal fun paramsGptSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    (keyStore.getKey(KEYSTORE_GPT_KEY_ALIAS, null) as? SecretKey)?.let { return it }
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    keyGenerator.init(
        KeyGenParameterSpec.Builder(
            KEYSTORE_GPT_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build(),
    )
    return keyGenerator.generateKey()
}

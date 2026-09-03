package dev.artplus.mobile

import android.content.SharedPreferences

/**
 * 内存 SharedPreferences，用于 JVM 单测 PresetStore / MainViewModel（无需 Robolectric）。
 */
class FakeSharedPreferences : SharedPreferences {

    private val data = HashMap<String, Any?>()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener?>()

    override fun getAll(): Map<String, *> = HashMap(data)

    override fun getString(key: String?, defValue: String?): String? =
        if (key != null && data.containsKey(key)) data[key] as? String else defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? =
        if (key != null && data.containsKey(key)) (data[key] as? Set<String>)?.toSet() else defValues

    override fun getInt(key: String?, defValue: Int): Int =
        if (key != null && data[key] is Int) data[key] as Int else defValue

    override fun getLong(key: String?, defValue: Long): Long =
        if (key != null && data[key] is Long) data[key] as Long else defValue

    override fun getFloat(key: String?, defValue: Float): Float =
        if (key != null && data[key] is Float) data[key] as Float else defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        if (key != null && data[key] is Boolean) data[key] as Boolean else defValue

    override fun contains(key: String?): Boolean = key != null && data.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        if (listener != null) {
            listeners += listener
        }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        listeners -= listener
    }

    private inner class FakeEditor : SharedPreferences.Editor {
        private val pending = HashMap<String, Any?>(data)
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor =
            apply { pending[key!!] = value }

        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor =
            apply { pending[key!!] = values?.toSet() }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor =
            apply { pending[key!!] = value }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor =
            apply { pending[key!!] = value }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor =
            apply { pending[key!!] = value }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor =
            apply { pending[key!!] = value }

        override fun remove(key: String?): SharedPreferences.Editor =
            apply { pending.remove(key) }

        override fun clear(): SharedPreferences.Editor = apply {
            clearAll = true
            pending.clear()
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) {
                data.clear()
                clearAll = false
            }
            val changedKeys = (data.keys + pending.keys).filter { data[it] != pending[it] }
            data.clear()
            data.putAll(pending)
            changedKeys.forEach { key ->
                listeners.forEach { it?.onSharedPreferenceChanged(this@FakeSharedPreferences, key) }
            }
        }
    }
}

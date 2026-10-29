package dev.artplus.morphdemo

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var componentBlue: ComponentName
    private lateinit var componentRed: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        componentBlue = ComponentName(this, "dev.artplus.morphdemo.MainActivity")
        componentRed = ComponentName(this, "dev.artplus.morphdemo.AliasRed")

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val btnBlue = findViewById<Button>(R.id.btn_switch_blue)
        val btnRed = findViewById<Button>(R.id.btn_switch_red)

        fun updateStatus() {
            val pm = packageManager
            val redState = pm.getComponentEnabledSetting(componentRed)
            val isRed = redState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            tvStatus.text = if (isRed) {
                "当前应用内别名状态：红色 (B-红已启用)"
            } else {
                "当前应用内别名状态：默认蓝色 (A-蓝已启用)"
            }
        }

        updateStatus()

        btnBlue.setOnClickListener {
            switchIcon(isRed = false)
            updateStatus()
            Toast.makeText(this, "应用已主动切换为蓝色图标 (A)", Toast.LENGTH_SHORT).show()
        }

        btnRed.setOnClickListener {
            switchIcon(isRed = true)
            updateStatus()
            Toast.makeText(this, "应用已主动切换为红色图标 (B)！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchIcon(isRed: Boolean) {
        val pm = packageManager
        if (isRed) {
            pm.setComponentEnabledSetting(
                componentBlue,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                componentRed,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        } else {
            pm.setComponentEnabledSetting(
                componentRed,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                componentBlue,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}

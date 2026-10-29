package dev.artplus.enhancer

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.preference.SwitchPreference
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import kotlin.concurrent.thread

/**
 * 桌面侧只认 LSPosed 的远程配置表，所以设置页必须经 `XposedService` 写；
 * 框架服务没绑上时退回本地文件，界面仍可用，只是同步不到桌面。
 */
object FrameworkBridge {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var service: XposedService? = null

    var connected by mutableStateOf(false)
        private set

    var prefsReady by mutableStateOf(false)
        private set

    var apiVersion by mutableStateOf(0)
        private set

    var frameworkName by mutableStateOf("")
        private set

    /** 每次绑定/断开自增，界面据此重读配置。 */
    var version by mutableIntStateOf(0)
        private set

    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        runCatching {
            XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    this@FrameworkBridge.service = service
                    val ready = ModulePrefs.attachService(service)
                    val name = runCatching { service.frameworkName }.getOrDefault("LSPosed")
                    val api = runCatching { service.apiVersion }.getOrDefault(102)
                    mainHandler.post {
                        connected = true
                        prefsReady = ready
                        frameworkName = name
                        apiVersion = api
                        version++
                    }
                }

                override fun onServiceDied(service: XposedService) {
                    if (this@FrameworkBridge.service === service) {
                        this@FrameworkBridge.service = null
                    }
                    ModulePrefs.attachService(null)
                    mainHandler.post {
                        connected = false
                        prefsReady = false
                        version++
                    }
                }
            })
        }
    }

    fun requestLauncherScope(onResult: (Boolean, String) -> Unit) {
        val svc = service
        if (svc == null) {
            onResult(false, "未连上 LSPosed 服务，请先在 LSPosed 里启用模块")
            return
        }
        runCatching {
            svc.requestScope(
                listOf("com.android.launcher", "com.oppo.launcher"),
                object : XposedService.OnScopeEventListener {
                    override fun onScopeRequestApproved(approvedScope: List<String>) {
                        mainHandler.post { onResult(true, "作用域已授权（${approvedScope.size} 项）") }
                    }

                    override fun onScopeRequestFailed(message: String) {
                        mainHandler.post { onResult(false, "作用域申请失败：$message") }
                    }
                },
            )
        }.onFailure { onResult(false, "作用域申请异常：${it.message}") }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ModulePrefs.setAppContext(applicationContext)
        FrameworkBridge.init()
        setContent {
            AppTheme {
                EnhancerScreen()
            }
        }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    COUITheme(
        colors = colors,
        content = content,
    )
}

@Composable
private fun EnhancerScreen() {
    val context = LocalContext.current
    val bridgeVersion = FrameworkBridge.version
    val scrollBehavior = COUIScrollBehavior()

    var dockGlass by remember { mutableStateOf(ModulePrefs.getBoolean(context, ModulePrefs.KEY_DOCK_GLASS, false)) }
    var dockBlur by remember { mutableStateOf(ModulePrefs.getBoolean(context, ModulePrefs.KEY_DOCK_BLUR, true)) }
    var iconRim by remember { mutableStateOf(ModulePrefs.getBoolean(context, ModulePrefs.KEY_ICON_RIM, false)) }
    var iconBody by remember { mutableStateOf(ModulePrefs.getBoolean(context, ModulePrefs.KEY_ICON_BODY, false)) }
    var rimLevel by remember { mutableStateOf(ModulePrefs.getInt(context, ModulePrefs.KEY_ICON_RIM_LEVEL, ModulePrefs.RIM_LEVEL_DEFAULT)) }
    var lightAngle by remember { mutableStateOf(ModulePrefs.getInt(context, ModulePrefs.KEY_ICON_LIGHT_ANGLE, ModulePrefs.LIGHT_ANGLE_DEFAULT)) }
    var morphLocal by remember { mutableStateOf(ModulePrefs.getBoolean(context, ModulePrefs.KEY_MORPH_LOCAL, true)) }

    // 框架服务绑定/断开后重读一遍：远程表拿到的值与本地表可能不同
    LaunchedEffect(bridgeVersion) {
        dockGlass = ModulePrefs.getBoolean(context, ModulePrefs.KEY_DOCK_GLASS, false)
        dockBlur = ModulePrefs.getBoolean(context, ModulePrefs.KEY_DOCK_BLUR, true)
        iconRim = ModulePrefs.getBoolean(context, ModulePrefs.KEY_ICON_RIM, false)
        iconBody = ModulePrefs.getBoolean(context, ModulePrefs.KEY_ICON_BODY, false)
        rimLevel = ModulePrefs.getInt(context, ModulePrefs.KEY_ICON_RIM_LEVEL, ModulePrefs.RIM_LEVEL_DEFAULT)
        lightAngle = ModulePrefs.getInt(context, ModulePrefs.KEY_ICON_LIGHT_ANGLE, ModulePrefs.LIGHT_ANGLE_DEFAULT)
        morphLocal = ModulePrefs.getBoolean(context, ModulePrefs.KEY_MORPH_LOCAL, true)
    }

    var scopeMessage by remember { mutableStateOf<String?>(null) }

    Coui17Scaffold(
        topBar = {
            Coui17TopAppBar(
                title = "ArtPlus 增强",
                scrollBehavior = scrollBehavior,
                actions = {
                    BarIconButton(
                        icon = EnhancerIcons.Refresh,
                        contentDescription = "强制关闭系统桌面并刷新",
                    ) {
                        restartLauncher(context)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            LazyColumn(
                contentPadding = innerPadding.listPadding(top = 16.dp, bottom = 48.dp),
            ) {
                item(key = "title_framework") {
                    GroupTitle("框架与作用域 (API 102)")
                }
                item(key = "card_framework") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        BasicComponent(
                            title = if (FrameworkBridge.connected) {
                                "${FrameworkBridge.frameworkName} 已连接"
                            } else {
                                "LSPosed 未连接"
                            },
                            summary = if (FrameworkBridge.connected) {
                                "API ${FrameworkBridge.apiVersion} · 作用域：系统桌面"
                            } else {
                                "未连上框架服务：开关写在本地，请先在 LSPosed 里启用并勾选系统桌面"
                            },
                            endActions = if (!FrameworkBridge.prefsReady) {
                                {
                                    TextButton(
                                        text = "申请作用域",
                                        onClick = {
                                            FrameworkBridge.requestLauncherScope { _, msg ->
                                                scopeMessage = msg
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = null,
                        )
                        scopeMessage?.let {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            Text(
                                text = it,
                                style = COUITheme.textStyles.body2,
                                color = COUITheme.colorScheme.onSurfaceSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                    }
                }

                item(key = "title_glass") {
                    GroupTitle("桌面液态玻璃")
                }
                item(key = "card_glass") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        SwitchPreference(
                            checked = dockGlass,
                            onCheckedChange = {
                                dockGlass = it
                                ModulePrefs.setBoolean(context, ModulePrefs.KEY_DOCK_GLASS, it)
                            },
                            title = "Dock 玻璃托盘",
                            summary = "给桌面底部 Dock 行添加圆角毛玻璃托盘",
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        SwitchPreference(
                            checked = dockBlur,
                            onCheckedChange = {
                                dockBlur = it
                                ModulePrefs.setBoolean(context, ModulePrefs.KEY_DOCK_BLUR, it)
                            },
                            title = "托盘模糊",
                            summary = "叠加桌面实时背景模糊；关闭后仅保留自适应半透明底色",
                            enabled = dockGlass,
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        SwitchPreference(
                            checked = iconRim,
                            onCheckedChange = {
                                iconRim = it
                                ModulePrefs.setBoolean(context, ModulePrefs.KEY_ICON_RIM, it)
                            },
                            title = "图标边缘光",
                            summary = "沿应用图标底板描一圈随光照渐变的高光",
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        SwitchPreference(
                            checked = iconBody,
                            onCheckedChange = {
                                iconBody = it
                                ModulePrefs.setBoolean(context, ModulePrefs.KEY_ICON_BODY, it)
                            },
                            title = "图标主体高光",
                            summary = "在底板之外为 Logo 本身再描一圈细高光（仿苹果）",
                            enabled = iconRim,
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        BasicComponent(
                            title = "边缘光强度",
                            summary = "点击循环切换强度",
                            endActions = {
                                Text(
                                    text = "$rimLevel%",
                                    style = COUITheme.textStyles.body2,
                                    color = if (iconRim) {
                                        COUITheme.colorScheme.onSurfaceSecondary
                                    } else {
                                        COUITheme.colorScheme.disabledOnSecondaryVariant
                                    },
                                )
                            },
                            onClick = {
                                rimLevel = when (rimLevel) {
                                    in 0..69 -> 80
                                    in 70..89 -> 100
                                    else -> 60
                                }
                                ModulePrefs.setInt(context, ModulePrefs.KEY_ICON_RIM_LEVEL, rimLevel)
                            },
                            enabled = iconRim,
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        BasicComponent(
                            title = "光源方向",
                            summary = "点击循环调整光线入射角",
                            endActions = {
                                Text(
                                    text = angleLabel(lightAngle),
                                    style = COUITheme.textStyles.body2,
                                    color = if (iconRim) {
                                        COUITheme.colorScheme.onSurfaceSecondary
                                    } else {
                                        COUITheme.colorScheme.disabledOnSecondaryVariant
                                    },
                                )
                            },
                            onClick = {
                                lightAngle = when (lightAngle) {
                                    90 -> 0
                                    0 -> 270
                                    270 -> 180
                                    else -> 90
                                }
                                ModulePrefs.setInt(context, ModulePrefs.KEY_ICON_LIGHT_ANGLE, lightAngle)
                            },
                            enabled = iconRim,
                        )
                    }
                }

                item(key = "title_morph") {
                    GroupTitle("可变图标接管")
                }
                item(key = "card_morph") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        SwitchPreference(
                            checked = morphLocal,
                            onCheckedChange = {
                                morphLocal = it
                                ModulePrefs.setBoolean(context, ModulePrefs.KEY_MORPH_LOCAL, it)
                            },
                            title = "使用 ArtPlus 本地图标",
                            summary = "带可变图标特性的应用不再向系统图标引擎取图，改用 ArtPlus 写入的本地素材",
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        Text(
                            text = "判定依据：/data/oplus/uxicons/<包名>/ 内存在 monochrome_light.png 或 " +
                                "monochrome_dark.png 即自动接管。系统图标引擎将被绕过，2x2 与 1x1 均稳定回落到 ArtPlus 本地素材路径。",
                            style = COUITheme.textStyles.body2,
                            color = COUITheme.colorScheme.onSurfaceSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }

                item(key = "title_actions") {
                    GroupTitle("快捷操作")
                }
                item(key = "card_actions") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        BasicComponent(
                            title = "强制关闭系统桌面",
                            summary = "检查 Root 权限并强制关闭桌面以立即刷新（必须在超级用户管理器中为本应用授予 Root 权限）",
                            onClick = { restartLauncher(context) },
                        )
                    }
                }

                item(key = "bottom_space") {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

private class RootCheck(val granted: Boolean, val message: String)

/** 严格检查 Root 权限。必须拿到超级用户授权才允许强关系统桌面。 */
private fun checkRootPermission(): RootCheck = runCatching {
    val process = ProcessBuilder("su", "-c", "id")
        .redirectErrorStream(true)
        .start()
    val finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
    if (!finished) {
        process.destroyForcibly()
        return RootCheck(false, "Root 权限申请超时：请在超级用户管理器中处理授权弹窗")
    }
    val output = process.inputStream.bufferedReader().readText().trim()
    val exitCode = process.exitValue()
    if (exitCode == 0 && output.contains("uid=0")) {
        RootCheck(true, "Root 权限已授予")
    } else {
        val denied = output.contains("denied", true) ||
            output.contains("not allowed", true) ||
            output.contains("not granted", true)
        val hint = if (denied) "Root 权限被拒绝" else "未授予 Root 权限"
        RootCheck(false, "$hint：必须在 KernelSU / Magisk / APatch 中为「ArtPlus 增强」授予超级用户权限，才能强制关闭桌面！")
    }
}.getOrElse {
    RootCheck(false, "未检测到 su：设备未 Root，或未在超级用户管理器中为本应用授予 Root 权限！")
}

/** 强制关闭系统桌面：必须检查 Root 权限，未授权时提示用户，已授权时执行强关。 */
private fun restartLauncher(context: Context) {
    Toast.makeText(context, "正在检查 Root 权限…", Toast.LENGTH_SHORT).show()
    thread(isDaemon = true) {
        val check = checkRootPermission()
        val handler = Handler(Looper.getMainLooper())
        if (!check.granted) {
            handler.post {
                Toast.makeText(context, check.message, Toast.LENGTH_LONG).show()
            }
            return@thread
        }

        handler.post {
            Toast.makeText(context, "已获取 Root 权限，正在强制关闭系统桌面…", Toast.LENGTH_SHORT).show()
        }

        val killScript = """
            am force-stop com.android.launcher 2>/dev/null
            kill -9 $(pidof com.android.launcher 2>/dev/null) 2>/dev/null
        """.trimIndent()

        val killed = runCatching {
            val p = ProcessBuilder("su", "-c", killScript)
                .redirectErrorStream(true)
                .start()
            val ok = p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            ok && p.exitValue() == 0
        }.getOrDefault(false)

        handler.post {
            if (killed) {
                Toast.makeText(context, "系统桌面已强制关闭并刷新", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "强制关闭桌面失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun angleLabel(angle: Int): String = when (angle) {
    90 -> "上方 (90°)"
    0 -> "右方 (0°)"
    270 -> "下方 (270°)"
    180 -> "左方 (180°)"
    else -> "$angle°"
}

package dev.artplus.mobile.ui.pages

import android.content.pm.ApplicationInfo
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.SmallTopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronBackward
import io.github.suqi8.coui.kmp.layout.DialogButtonBarAction
import io.github.suqi8.coui.kmp.theme.COUITheme
import dev.artplus.mobile.GITHUB_LICENSE_URL
import dev.artplus.mobile.GITHUB_REPO_URL
import dev.artplus.mobile.GPL_LICENSE_ASSET_PATH
import dev.artplus.mobile.GPL_LICENSE_FALLBACK_TEXT
import dev.artplus.mobile.MainActivity
import dev.artplus.mobile.state.setStatus
import dev.artplus.mobile.ui.cards.AppPage
import dev.artplus.mobile.ui.components.ArtDialog
import dev.artplus.mobile.ui.components.LibrarySettingRow
import dev.artplus.mobile.ui.components.SectionCard
import dev.artplus.mobile.ui.components.sectionItem
import dev.artplus.mobile.ui.components.SectionDivider
import dev.artplus.mobile.ui.components.SettingsIconKind
import dev.artplus.mobile.ui.components.TitleBarIconButton
import dev.artplus.mobile.ui.components.rememberLinkedTopAppBarScrollBehavior
import dev.artplus.mobile.ui.pages.picker.pickerCheckForUpdate
import dev.artplus.mobile.ui.pages.picker.pickerCurrentVersionName
import dev.artplus.mobile.ui.pages.picker.pickerFetchUpdateBody
import dev.artplus.mobile.ui.pages.picker.pickerOpenExternalLink
import dev.artplus.mobile.ui.pages.picker.pickerResolveUpdateUrl
import dev.artplus.mobile.ui.pages.picker.pickerToastStatus
import dev.artplus.mobile.R

@Composable
internal fun MainActivity.AboutPage(pageBackground: Color) {
    val scrollBehavior = rememberLinkedTopAppBarScrollBehavior()
    val shell by mainViewModel.shell.collectAsState()
    val updateUi by mainViewModel.updateUi.collectAsState()
    val aboutCtx = LocalContext.current
    val versionName = pickerCurrentVersionName(
        getVersionName = {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName
        },
    )
    val versionCode = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode.toString()
        }
    } catch (_: Exception) {
        ""
    }

    // Backdrop for the ColorOS app bar material: the list only, never the bar itself.
    val appBarBackdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }
    val contentTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.SmallTopAppBarCenterHeight
    Scaffold(
        // 内容从顶栏底下滚过（顶部留白在 contentPadding 里），COUI 才会给顶栏画玻璃材质。
        edgeToEdge = true,
        containerColor = pageBackground,
        topBar = {
            CompositionLocalProvider(LocalBackdrop provides appBarBackdrop) {
                // 带返回键的二级页：COUI 的固定小标题栏（18dp Medium），与 demo 的二级页一致。
                SmallTopAppBar(
                    title = "关于",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        TitleBarIconButton(
                            icon = COUIIcons.ChevronBackward,
                            contentDescription = "返回",
                            enabled = !shell.isBusy,
                            onClick = { mainViewModel.updateShell { it -> it.copy(currentPage = (AppPage.Home)) } },
                        )
                    },
                    gradientGlass = true,
                    showDivider = false,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(appBarBackdrop)
                .background(pageBackground)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = contentTopInset + 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            sectionItem(key = "about_app", title = "应用信息") {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = "ArtPlus",
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)),
                        )
                        Text(
                            text = "ArtPlus Mobile",
                            style = COUITheme.textStyles.title3.copy(fontWeight = FontWeight.Bold),
                            color = COUITheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (versionCode.isNotBlank()) "$versionName ($versionCode)" else versionName,
                            style = COUITheme.textStyles.body2,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "ColorOS ART+ 图标生成与预览工具",
                            style = COUITheme.textStyles.footnote1,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item {
                SectionCard(rowsFullBleed = true, title = "链接") {
                    LibrarySettingRow(
                        title = "GitHub 仓库",
                        summary = GITHUB_REPO_URL,
                        icon = SettingsIconKind.Link,
                        showArrowRight = true,
                        enabled = !shell.isBusy,
                        onClick = {
                            pickerOpenExternalLink(
                                start = ::startActivity,
                                url = GITHUB_REPO_URL,
                                onError = { msg ->
                                    pickerToastStatus(
                                        message = msg,
                                        postOnUi = { setStatus((it)) },
                                        showToast = { Toast.makeText(aboutCtx, it, Toast.LENGTH_SHORT).show() },
                                    )
                                },
                            )
                        },
                    )
                    SectionDivider()
                    LibrarySettingRow(
                        title = "开源协议",
                        summary = "GPL-3.0-or-later",
                        icon = SettingsIconKind.Shield,
                        showArrowRight = true,
                        enabled = !shell.isBusy,
                        onClick = { mainViewModel.updateUpdateUi { it -> it.copy(gplLicenseDialogVisible = (true)) } },
                    )
                    SectionDivider()
                    LibrarySettingRow(
                        title = "检查更新",
                        summary = if (updateUi.isCheckingUpdate) "检查中..." else "当前 $versionName",
                        icon = SettingsIconKind.Grid,
                        showArrowRight = !updateUi.isCheckingUpdate,
                        enabled = !shell.isBusy && !updateUi.isCheckingUpdate,
                        onClick = {
                            pickerCheckForUpdate(
                                isChecking = updateUi.isCheckingUpdate,
                                onCheckingChange = { mainViewModel.updateUpdateUi { v -> v.copy(isCheckingUpdate = (it)) } },
                                onStatusText = { setStatus((it)) },
                                scope = mainScope,
                                resolveUrl = { pickerResolveUpdateUrl(it, "检查更新", (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) },
                                fetchLatest = ::pickerFetchUpdateBody,
                                currentVersion = versionName,
                                onUpdateAvailable = { info, text ->
                                    mainViewModel.updateUpdateUi { it -> it.copy(updateAvailableInfo = (info)) }
                                    setStatus((text))
                                },
                                onUpToDate = {
                                    mainViewModel.updateUpdateUi { it -> it.copy(updateUpToDateDialogVisible = (true)) }
                                    setStatus((it))
                                },
                                onFailed = { setStatus((it)) },
                            )
                        },
                    )
                }
            }
            item {
                Text(
                    text = "© 2026 Costben · GPL-3.0-or-later",
                    style = COUITheme.textStyles.footnote1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }

    if (updateUi.gplLicenseDialogVisible) {
        val dismissLicense = { mainViewModel.updateUpdateUi { it -> it.copy(gplLicenseDialogVisible = (false)) } }
        ArtDialog(
            title = "开源协议",
            summary = "GPL-3.0-or-later",
            onDismissRequest = dismissLicense,
            negative = DialogButtonBarAction(text = "关闭", onClick = dismissLicense),
            positive = DialogButtonBarAction(
                text = "在 GitHub 查看",
                recommended = true,
                onClick = {
                    dismissLicense()
                    pickerOpenExternalLink(
                        start = ::startActivity,
                        url = GITHUB_LICENSE_URL,
                        onError = { setStatus((it)) },
                    )
                },
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 360.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(COUITheme.colorScheme.secondaryVariant)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                Text(
                    text = remember(aboutCtx) {
                        runCatching {
                            aboutCtx.assets.open(GPL_LICENSE_ASSET_PATH).bufferedReader().readText()
                        }.getOrDefault(GPL_LICENSE_FALLBACK_TEXT)
                    },
                    style = COUITheme.textStyles.footnote2,
                    color = COUITheme.colorScheme.onSurface,
                )
            }
        }
    }

    updateUi.updateAvailableInfo?.let { info ->
        val dismissUpdate = { mainViewModel.updateUpdateUi { it -> it.copy(updateAvailableInfo = (null)) } }
        ArtDialog(
            title = "发现新版本",
            summary = "最新 ${info.tagName} · 当前 $versionName\n点击前往下载页查看更新内容。",
            onDismissRequest = dismissUpdate,
            centered = true,
            negative = DialogButtonBarAction(text = "稍后", onClick = dismissUpdate),
            positive = DialogButtonBarAction(
                text = "前往下载",
                recommended = true,
                onClick = {
                    val url = info.htmlUrl
                    dismissUpdate()
                    pickerOpenExternalLink(
                        start = ::startActivity,
                        url = url,
                        onError = { setStatus((it)) },
                    )
                },
            ),
        )
    }

    if (updateUi.updateUpToDateDialogVisible) {
        val dismissUpToDate = { mainViewModel.updateUpdateUi { it -> it.copy(updateUpToDateDialogVisible = (false)) } }
        ArtDialog(
            title = "已是最新版本",
            summary = "当前 $versionName 已是最新，无需更新。",
            onDismissRequest = dismissUpToDate,
            centered = true,
            positive = DialogButtonBarAction(text = "知道了", recommended = true, onClick = dismissUpToDate),
        )
    }
}

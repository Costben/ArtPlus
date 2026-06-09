package dev.artplus.mobile

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import kotlin.math.pow
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {
    private val apps = mutableStateListOf<AppEntry>()
    private var queryText by mutableStateOf("")
    private var selectedPackageName by mutableStateOf<String?>(null)
    private var statusText by mutableStateOf("加载应用列表中...")
    private var outputTreeUri by mutableStateOf<Uri?>(null)
    private var isBusy by mutableStateOf(false)

    private val chooseTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) {
                statusText = "未选择输出目录"
                return@registerForActivityResult
            }
            outputTreeUri = uri
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            statusText = "已选择输出目录"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ArtPlus Mobile"

        setContent {
            val darkMode = isSystemInDarkTheme()

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkMode },
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                onDispose {}
            }

            MiuixTheme(
                colors = if (darkMode) darkColorScheme() else lightColorScheme(),
            ) {
                ArtPlusScreen()
            }
        }

        loadApps()
    }

    @Composable
    private fun ArtPlusScreen() {
        val selectedApp = apps.firstOrNull { it.packageName == selectedPackageName }
        val filteredApps = apps.filter { entry ->
            val query = queryText.trim().lowercase(Locale.ROOT)
            query.isEmpty() ||
                entry.label.lowercase(Locale.ROOT).contains(query) ||
                entry.packageName.lowercase(Locale.ROOT).contains(query)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .padding(horizontal = 18.dp)
                .padding(top = 28.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "ArtPlus Mobile",
                style = MiuixTheme.textStyles.title1,
                color = MiuixTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "选择手机里的应用，生成本地 ART+ 图标资源。",
                style = MiuixTheme.textStyles.paragraph,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
            )

            StatusCard(selectedApp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = "选择目录",
                    onClick = { chooseTreeLauncher.launch(null) },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { generateSelected(installWithRoot = false) },
                    enabled = selectedApp != null && !isBusy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = "生成本地版",
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }

            Button(
                onClick = { generateSelected(installWithRoot = true) },
                enabled = selectedApp != null && !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "生成并尝试 Root 写入",
                    style = MiuixTheme.textStyles.button,
                )
            }

            TextField(
                value = queryText,
                onValueChange = { queryText = it },
                label = "搜索应用或包名",
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "应用列表",
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${filteredApps.size}/${apps.size}",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp),
            ) {
                items(filteredApps, key = { it.packageName }) { entry ->
                    AppRow(
                        entry = entry,
                        selected = entry.packageName == selectedPackageName,
                        onClick = {
                            selectedPackageName = entry.packageName
                            statusText = "已选择: ${entry.label} (${entry.packageName})"
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun StatusCard(selectedApp: AppEntry?) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
            colors = CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Text(
                text = statusText,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedApp?.packageName ?: "尚未选择应用",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val outputText = if (outputTreeUri == null) {
                "输出到应用私有目录；选择目录后会同步导出"
            } else {
                "已启用外部目录导出"
            }
            Text(
                text = outputText,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }

    @Composable
    private fun AppRow(entry: AppEntry, selected: Boolean, onClick: () -> Unit) {
        val color = if (selected) {
            MiuixTheme.colorScheme.primaryVariant
        } else {
            MiuixTheme.colorScheme.surfaceContainer
        }
        val titleColor = if (selected) {
            MiuixTheme.colorScheme.onPrimaryVariant
        } else {
            MiuixTheme.colorScheme.onSurfaceContainer
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(14.dp),
            colors = CardDefaults.defaultColors(color = color),
            pressFeedbackType = PressFeedbackType.Sink,
            showIndication = true,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                        style = MiuixTheme.textStyles.title4,
                        color = MiuixTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = entry.label,
                        style = MiuixTheme.textStyles.body1,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = entry.packageName,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    private fun loadApps() {
        Thread {
            val pm = packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            val unique = linkedMapOf<String, AppEntry>()

            for (info in resolveInfos) {
                val activityInfo = info.activityInfo ?: continue
                val packageName = activityInfo.packageName
                val label = info.loadLabel(pm)?.toString() ?: packageName
                unique[packageName] = AppEntry(label, packageName, info)
            }

            val sorted = unique.values.sortedBy { it.label.lowercase(Locale.ROOT) }
            runOnUiThread {
                apps.clear()
                apps.addAll(sorted)
                statusText = "共 ${apps.size} 个启动器应用。选择一个后生成。"
            }
        }.start()
    }

    private fun generateSelected(installWithRoot: Boolean) {
        val entry = apps.firstOrNull { it.packageName == selectedPackageName }
        if (entry == null) {
            statusText = "先选择一个应用"
            return
        }
        if (isBusy) {
            return
        }

        isBusy = true
        statusText = "处理中: ${entry.packageName}"
        Thread {
            try {
                val outDir = generateArtPlusPackage(entry)
                if (outputTreeUri != null) {
                    exportToTree(outDir)
                }
                if (installWithRoot) {
                    installWithRoot(outDir, entry.packageName)
                    status("已生成并尝试 Root 写入: ${entry.packageName}")
                } else {
                    status("已生成: ${outDir.absolutePath}")
                }
            } catch (error: Exception) {
                status("失败: ${error.message ?: error.javaClass.simpleName}")
            } finally {
                runOnUiThread { isBusy = false }
            }
        }.start()
    }

    private fun generateArtPlusPackage(app: AppEntry): File {
        val base = getExternalFilesDir("ArtPlus") ?: File(filesDir, "ArtPlus")
        val outDir = File(base, app.packageName)
        ensureCleanDir(outDir)

        val icon = app.resolveInfo.loadIcon(packageManager)
        val recfg: Bitmap
        val recbg: Bitmap
        if (icon is AdaptiveIconDrawable) {
            recfg = drawDrawable(icon.foreground, SIZE_1X1, SIZE_1X1, transparent = true)
            recbg = drawDrawable(
                icon.background ?: ColorDrawable(AndroidColor.WHITE),
                SIZE_1X1,
                SIZE_1X1,
                transparent = false,
            )
        } else {
            recfg = drawDrawable(icon, SIZE_1X1, SIZE_1X1, transparent = true)
            recbg = solidBitmap(SIZE_1X1, SIZE_1X1, AndroidColor.WHITE)
        }

        savePng(recbg, File(outDir, "recbg.png"))
        savePng(recfg, File(outDir, "recfg.png"))
        savePng(resizeBitmap(recbg, SIZE_1X2[0], SIZE_1X2[1]), File(outDir, "recbg_1x2.png"))
        savePng(resizeBitmap(recbg, SIZE_2X1[0], SIZE_2X1[1]), File(outDir, "recbg_2x1.png"))
        savePng(resizeBitmap(recbg, SIZE_2X2, SIZE_2X2), File(outDir, "recbg_2x2.png"))

        val recfg1x2 = centerOnCanvas(recfg, SIZE_1X2[0], SIZE_1X2[1])
        val recfg2x1 = centerOnCanvas(recfg, SIZE_2X1[0], SIZE_2X1[1])
        val recfg2x2 = centerOnCanvas(recfg, SIZE_2X2, SIZE_2X2)
        savePng(recfg1x2, File(outDir, "recfg_1x2.png"))
        savePng(recfg2x1, File(outDir, "recfg_2x1.png"))
        savePng(recfg2x2, File(outDir, "recfg_2x2.png"))

        val bgColor = sampleColor(recbg)
        savePng(recolorAlpha(recfg, bgColor), File(outDir, "rec_night.png"))
        savePng(recolorAlpha(recfg1x2, bgColor), File(outDir, "rec_night_1x2.png"))
        savePng(recolorAlpha(recfg2x1, bgColor), File(outDir, "rec_night_2x1.png"))
        savePng(recolorAlpha(recfg2x2, bgColor), File(outDir, "rec_night_2x2.png"))

        savePng(monochromeAlpha(recfg), File(outDir, "monochrome.png"))
        savePng(monochromeAlpha(recfg1x2), File(outDir, "monochrome_1x2.png"))
        savePng(monochromeAlpha(recfg2x1), File(outDir, "monochrome_2x1.png"))
        savePng(monochromeAlpha(recfg2x2), File(outDir, "monochrome_2x2.png"))

        savePng(adjustColor(recfg, 1.3f, 1.0f), File(outDir, "day.png"))
        savePng(adjustColor(recfg, 0.9f, 0.9f), File(outDir, "nsd.png"))
        savePng(adjustColor(recfg, 0.9f, 1.05f), File(outDir, "mat.png"))
        savePng(adjustColor(recfg, 0.7f, 0.95f), File(outDir, "peb.png"))
        return outDir
    }

    private fun drawDrawable(
        drawable: Drawable?,
        width: Int,
        height: Int,
        transparent: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(if (transparent) AndroidColor.TRANSPARENT else AndroidColor.WHITE)
        if (drawable != null) {
            val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
            copy.setBounds(0, 0, width, height)
            copy.draw(canvas)
        }
        return bitmap
    }

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(color)
        return bitmap
    }

    private fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(source, width, height, true)

    private fun centerOnCanvas(source: Bitmap, width: Int, height: Int): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(AndroidColor.TRANSPARENT)
        val x = (width - source.width) / 2f
        val y = (height - source.height) / 2f
        canvas.drawBitmap(source, x, y, null)
        return out
    }

    private fun recolorAlpha(source: Bitmap, color: Int): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val rgb = color and 0x00ffffff
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val alpha = AndroidColor.alpha(source.getPixel(x, y))
                out.setPixel(x, y, (alpha shl 24) or rgb)
            }
        }
        return out
    }

    private fun monochromeAlpha(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val lumas = sourcePixels
            .filter { AndroidColor.alpha(it) > 8 }
            .map { luma(it) }
            .toMutableList()
        val low = percentile(lumas, 0.02)
        val high = percentile(lumas, 0.98)
        val hasRange = high - low >= 12
        val outPixels = IntArray(sourcePixels.size)

        for (i in sourcePixels.indices) {
            val pixel = sourcePixels[i]
            val alpha = AndroidColor.alpha(pixel)
            if (alpha <= 0) {
                outPixels[i] = AndroidColor.TRANSPARENT
                continue
            }
            val maskAlpha = if (hasRange) {
                val normalized = ((luma(pixel) - low).toDouble() / (high - low).toDouble())
                    .coerceIn(0.0, 1.0)
                    .pow(MONO_ALPHA_GAMMA)
                MONO_ALPHA_MIN + normalized * (MONO_ALPHA_MAX - MONO_ALPHA_MIN)
            } else {
                MONO_ALPHA_MAX.toDouble()
            }
            val outAlpha = ((alpha / 255.0) * maskAlpha).toInt().coerceIn(0, 255)
            outPixels[i] = (outAlpha shl 24) or 0x00ffffff
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun percentile(values: MutableList<Int>, ratio: Double): Int {
        if (values.isEmpty()) {
            return 0
        }
        values.sort()
        val index = ((values.size - 1) * ratio)
            .toInt()
            .coerceIn(0, values.size - 1)
        return values[index]
    }

    private fun luma(pixel: Int): Int =
        (AndroidColor.red(pixel) * 0.299 +
            AndroidColor.green(pixel) * 0.587 +
            AndroidColor.blue(pixel) * 0.114).toInt()

    private fun sampleColor(bitmap: Bitmap): Int {
        val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        if (
            AndroidColor.alpha(center) > 32 &&
            AndroidColor.red(center) + AndroidColor.green(center) + AndroidColor.blue(center) >= 120
        ) {
            return AndroidColor.rgb(
                AndroidColor.red(center),
                AndroidColor.green(center),
                AndroidColor.blue(center),
            )
        }

        var red = 0L
        var green = 0L
        var blue = 0L
        var count = 0L
        for (y in 0 until bitmap.height step 8) {
            for (x in 0 until bitmap.width step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (AndroidColor.alpha(pixel) >= 128) {
                    red += AndroidColor.red(pixel)
                    green += AndroidColor.green(pixel)
                    blue += AndroidColor.blue(pixel)
                    count++
                }
            }
        }
        if (count == 0L) {
            return AndroidColor.rgb(216, 224, 253)
        }
        return AndroidColor.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
    }

    private fun adjustColor(source: Bitmap, saturation: Float, brightness: Float): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        saturationMatrix.postConcat(brightnessMatrix)
        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun savePng(bitmap: Bitmap, file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            error("无法创建目录: $parent")
        }
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun ensureCleanDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
            return
        }
        dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".png") }
            ?.forEach { it.delete() }
    }

    private fun exportToTree(packageDir: File) {
        val treeUri = outputTreeUri ?: return
        val rootDoc = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        var packageDoc = findChild(treeUri, rootDoc, packageDir.name)
        if (packageDoc == null) {
            packageDoc = DocumentsContract.createDocument(
                contentResolver,
                rootDoc,
                DocumentsContract.Document.MIME_TYPE_DIR,
                packageDir.name,
            )
        }
        if (packageDoc == null) {
            error("无法创建输出目录")
        }

        val files = packageDir.listFiles { _, name -> name.endsWith(".png") } ?: return
        for (file in files) {
            findChild(treeUri, packageDoc, file.name)?.let {
                DocumentsContract.deleteDocument(contentResolver, it)
            }
            val doc = DocumentsContract.createDocument(contentResolver, packageDoc, "image/png", file.name)
                ?: error("无法创建文件: ${file.name}")
            FileInputStream(file).use { input ->
                contentResolver.openOutputStream(doc, "w").useRequired { output ->
                    copyStream(input, output)
                }
            }
        }
    }

    private fun findChild(treeUri: Uri, parentDoc: Uri, displayName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getDocumentId(parentDoc),
        )
        return try {
            contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childName = cursor.getString(1)
                    if (displayName == childName) {
                        val documentId = cursor.getString(0)
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun installWithRoot(packageDir: File, packageName: String) {
        val target = "/data/oplus/uxicons/$packageName"
        val source = packageDir.absolutePath
        val command = """
            set -e
            mkdir -p ${shQuote(target)}
            cp -f ${shQuote(source)}/*.png ${shQuote(target)}/
            chmod 0644 ${shQuote(target)}/*.png
            restorecon -RF ${shQuote(target)} 2>/dev/null || true
        """.trimIndent()
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val code = process.waitFor()
        if (code != 0) {
            error("su 退出码: $code")
        }
    }

    private fun shQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun status(message: String) {
        runOnUiThread { statusText = message }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                return
            }
            output.write(buffer, 0, read)
        }
    }

    private fun OutputStream?.useRequired(block: (OutputStream) -> Unit) {
        val output = this ?: error("无法打开输出流")
        output.use { block(it) }
    }

    private data class AppEntry(
        val label: String,
        val packageName: String,
        val resolveInfo: ResolveInfo,
    )

    companion object {
        private const val SIZE_1X1 = 240
        private const val SIZE_2X2 = 704
        private val SIZE_1X2 = intArrayOf(240, 820)
        private val SIZE_2X1 = intArrayOf(820, 240)
        private const val MONO_ALPHA_MIN = 40
        private const val MONO_ALPHA_MAX = 230
        private const val MONO_ALPHA_GAMMA = 0.85
    }
}

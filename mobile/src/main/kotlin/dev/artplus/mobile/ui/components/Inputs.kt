package dev.artplus.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.SliderDefaults
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.couiTopAppBarActionItem
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.TextFieldMode

@Composable
internal fun SteppedPercentSlider(
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    showDots: Boolean = true,
    onValueChange: (Int) -> Unit,
) {
    val safeMin = min.toFloat()
    val safeMax = max.coerceAtLeast(min + 1).toFloat()
    val safeStep = step.coerceAtLeast(1)
    val rangeSpan = (max - min).coerceAtLeast(1)
    val stepCount = rangeSpan / safeStep
    val steps = (stepCount - 1).coerceAtLeast(0)
    val showKeyPoints = showDots && stepCount in 1..30
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Slider(
        value = value.coerceIn(min, max).toFloat(),
        onValueChange = { floatVal ->
            val rawIndex = ((floatVal - safeMin) / safeStep.toFloat()).roundToInt()
            val nextValue = (min + rawIndex * safeStep).coerceIn(min, max)
            if (nextValue != value) {
                currentOnValueChange(nextValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        valueRange = safeMin..safeMax,
        steps = steps,
        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
        showKeyPoints = showKeyPoints,
    )
}

@Composable
internal fun NumberInputBox(
    value: String,
    fallbackValue: Int,
    min: Int,
    max: Int,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: (String) -> Unit,
    backgroundColor: Color? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(focused) {
        if (focused) {
            delay(260)
            bringIntoViewRequester.bringIntoView()
        }
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(36.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(11.dp))
            .background(backgroundColor ?: COUITheme.colorScheme.secondaryVariant)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { newValue ->
                val maxLen = max.toString().length.coerceAtLeast(1).coerceAtMost(3)
                var digits = newValue.text.filter { it.isDigit() }.take(maxLen)
                // 只能填写规定范围内：实时钳到 max，min 仅在 Done 时钳制，避免输入过程中误拦（如 min 45 时输 4）
                if (digits.isNotEmpty()) {
                    digits.toIntOrNull()?.let { v ->
                        if (v > max) digits = max.toString()
                    }
                }
                if (digits != newValue.text) {
                    val sel = digits.length
                    fieldValue = TextFieldValue(digits, TextRange(sel))
                    onValueChange(digits)
                } else {
                    val selStart = newValue.selection.start.coerceIn(0, digits.length)
                    val selEnd = newValue.selection.end.coerceIn(0, digits.length)
                    fieldValue = newValue.copy(text = digits, selection = TextRange(selStart, selEnd))
                    onValueChange(digits)
                }
            },
            enabled = enabled,
            singleLine = true,
            textStyle = COUITheme.textStyles.body1.copy(
                color = COUITheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None,
                ),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone(fieldValue.text)
                    focusManager.clearFocus()
                },
            ),
            cursorBrush = SolidColor(COUITheme.colorScheme.primaryVariant),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    } else {
                        if (fieldValue.text.isEmpty()) {
                            val fb = fallbackValue.toString()
                            fieldValue = TextFieldValue(fb, TextRange(fb.length))
                            onValueChange(fb)
                        }
                    }
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

@Composable
internal fun CompactActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    // 三等分窄按钮可收窄水平内边距，避免 4 个汉字被默认 12dp 内边距挤成两行。
    insideMargin: PaddingValues = ButtonDefaults.InsideMargin,
) {
    // COUI TextButton：中性 secondaryVariant 填充、药丸圆角、按压缩放与禁用态均为原生。
    TextButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(),
        insideMargin = insideMargin,
    )
}

@Composable
internal fun InlineInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    obscure: Boolean = false,
    icon: SettingsIconKind? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(focused) {
        if (focused) {
            delay(260)
            bringIntoViewRequester.bringIntoView()
        }
    }

    // ColorOS 对话框输入形态：Line 模式（底部细线 + 聚焦展开线），自带清除键与密码显隐。
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focused = it.isFocused },
        backgroundMode = TextFieldMode.Line,
        label = label,
        singleLine = true,
        showClearButton = true,
        showPasswordToggle = obscure,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        leadingIcon = icon?.let { kind -> { SettingsLineIcon(kind = kind) } },
    )
}

/**
 * Top app bar icon key: COUI's ColorOS 17 [HighlightedIconButton] drawing a 24dp COUI icon.
 * Callers pass a `COUIIcons` vector; COUI's TopAppBar already supplies the 16dp toolbar margins.
 */
@Composable
internal fun TitleBarIconButton(
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    HighlightedIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.couiTopAppBarActionItem(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun LiquidGlassSwitch(checked: Boolean, enabled: Boolean) {
    // 历史命名：实为普通开关，使用 COUI 原生 Switch（行自身接管点击）。
    Switch(
        checked = checked,
        onCheckedChange = null,
        enabled = enabled,
    )
}

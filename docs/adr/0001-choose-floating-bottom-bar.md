# Choose KernelSU FloatingBottomBar 1:1 (vendored miuix-blur 0.9.1)

Context: ArtPlus 首页需 1:1 复刻 KernelSU 管理器的 iOS 液态玻璃悬浮底栏（vibrancy + blur 4dp + lens 24dp 折射 + BloomStroke 双光源高光 + 拖拽阻尼换页）。此前 `backdrop:1.0.6` 简化版只有 `blur 8dp`，缺少 lens 折射、vibrancy、高光、活动 tab「液体透镜」，观感不像 iOS。

调研结论:
- KernelSU 实际依赖 miuix-blur 0.9.3（vendored Kyant backdrop + highlight/sensor 扩展），技术栈为 AGP 9.3 + Kotlin 2.4 + compileSdk 37；
- `io.github.kyant0:backdrop` 2.0.1 与 miuix-blur ≥0.9.1 的 AAR 均要求 compileSdk 37 / AGP 9.x（Android 17 minor-SDK 新命名，AGP 8.9.1 无法识别 android-37.0 平台）；
- miuix-blur 0.9.1 的 API 与 KernelSU 源码完全对上（`blur(radiusX, radiusY)`、`runtimeShaderEffect`、`isRuntimeShaderSupported`、`highlight.BloomStroke/LightSource/LightPosition`、`sensor.rememberDeviceTilt`），且 Kotlin 2.3.21 / foundation 1.11.0 与现有 Kotlin 2.3.20 兼容。

Decision:
- 不升级 AGP/compileSdk，保持 compileSdk 36 + AGP 8.9.1 + Kotlin 2.3.20（避免 OnePlus 工具链大面积迁移风险）。
- 将 miuix-blur 0.9.1 的 blur 模块源码（Apache-2.0，commonMain+androidMain 共 27 个文件）vendor 进 `mobile/src/main/kotlin/top/yukonga/miuix/kmp/blur/`，扁平化 KMP expect/actual 为单平台实现；编译器加 `-Xcontext-parameters`。
- 从 KernelSU（Apache-2.0）拷入 `dev.artplus.mobile.glass`：FloatingBottomBar.kt（三层玻璃+阻尼拖拽+重力高光）、DampedDragAnimation、InteractiveHighlight、DragGestureInspector、Lens.kt（AGSL 折射）、Vibrancy.kt、InnerShadow.kt、CombinedBackdrop.kt。
- HomePage 用 `FloatingBottomBar(backdrop, 3 tabs)` 替换旧简化底栏；设置页的两开关（悬浮底栏/底栏模糊）语义保留：模糊开=三层玻璃，关=实色胶囊（KernelSU 分支）。

Consequences:
- 设备 PLK110 / Android 16 / API 36 实测 AGSL 可用：无 SIGSEGV、拖拽换页正常、活动 pill 高光+折射正常。
- lens/highlight 由 `isRuntimeShaderSupported()` 守卫（API 33 以下自动降级为 blur-only）。
- 生成设置/预设/对比条仍保持实色不透（出范围）。

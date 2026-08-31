# Choose FloatingBottomBar 1:1 from KernelSU

Context: ArtPlus 首页需 1:1 复刻 KernelSU 管理器的悬浮底栏（含点击放大回弹的玻璃动效）。Kyant backdrop:1.0.6 的简化版在 compileSdk 36 上稳定但曾丢失放大回弹；完整 1:1 需 miuix-blur 0.9.3 + compileSdk 37 会在 OnePlus 上触发 RenderThread SIGSEGV。

Decision: 保持 compileSdk 36 + miuix-android 0.8.8 + backdrop 1.0.6 + shapes 1.2.0 保证 OnePlus 稳定。HomePage 使用 `rememberLayerBackdrop + layerBackdrop + drawBackdrop(Capsule, blur 8dp)` 复刻 KernelSU 的 Capsule + 容器 0.4 样式，去掉易崩的 lens/vibrancy；保留 `animateFloatAsState(1.15f, spring 0.55/420)` 的玻璃放大回弹。设置提供双开关 `悬浮底栏`/`底栏模糊` 与 KernelSU 一致：`isBlurEnabled = 悬浮 && 模糊`，关闭模糊时保留悬浮胶囊的实色与动画，关闭悬浮时退化为贴底直条。

Consequences: 不升级 AGP/compileSdk，1:1 视觉由 Capsule+blur8dp 近似；动效保留；熔断避免 SIGSEGV；生成设置与对比条保持实色不透。

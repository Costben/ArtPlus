# 设置页选择组件库化（全库规范）

设置页的「选择类行 + 弹窗」（接口选择、AI 提示词等）原本为自绘实现（ChoicePopup*），呈现出容器圆角误解、选中色块缝隙/弧度、行高不一等与库标准偏离的缺陷。2026-08 工具链升级（AGP 9.3 / Kotlin 2.4 / miuix 0.9.3）后存在两条路径，最终拍板：**整个设置页 UI 一律按 miuix 库规范**，不再保留自绘皮肤。

采用库规范：

- 组装形式：`Scaffold` + `Card` + preference 组件（`OverlayDropdownPreference` / `SwitchPreference` / `CheckboxPreference`），顶栏 `TopAppBar`、按钮 `Button`。
- 选择弹窗：`OverlayDropdownPopup` 标准容器（圆角 16dp、宽度 200–288dp、dim、入场动画、haptic、支持分组分隔线）；行高遵循库标准（首末 20dp / 中间 12dp），不再追求统一行高。
- 唯一定制：`dropdownColors` 选中态 = 整行 `primaryVariant@12%` 蓝块 + 蓝字 + 蓝勾（颜色保持主题蓝；色块机制仍为库行为：铺满整行、由容器圆角裁切）。
- 设置行展开箭头：`ArrowUpDown` 10×16dp 同色一体（单路径填充，无重叠亮斑）。
- 文本输入设置（模型 ID / Base URL / API key）：点击设置行 → 库 `Dialog` + `TextField` 输入弹窗，行内仅显示当前值。
- 自绘弹窗/行皮肤代码整体删除；首页与悬浮底栏不在本次范围。

**Considered Options**: 保留自绘 `ChoicePopup` 并按 8 项细则微调（可保留统一 12dp 行高、任意细节定制），因「整个 UI 完全按库规范」被拒——统一行高这一条随之放弃。

**Consequences**: 设置行视觉/动效/触感全部跟随库版本；删除 `ChoicePopupOverlay` / `ChoicePopupOptionRow` / `ChoicePopupChevron` / `InlineInputField` 等代码；后续升级 miuix 时设置页零维护。

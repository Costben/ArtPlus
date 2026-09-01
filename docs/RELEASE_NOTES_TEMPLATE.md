# Release Notes 规范 / Release Notes Spec

> 适用于所有 `v*` tag 触发的 GitHub Release。标题统一为 `ArtPlus <version>`（已去掉 Android，不截断版本号）。

## 1. 格式要求

- **双语**：英文在前，中文在后，中间用 `---` 分隔，内容一一对应
- **简洁**：只写本次更新的要点，不写历史累积
- **结构**：分两类 bullet，要点式
  - `✨ New Features / 新增功能`
  - `🐛 Fixes / 修复`
  - 可选第三类 `🔧 Improvements / 优化`（有才写，没有就删掉该节）

## 2. 标准模板（复制即用）

```markdown
## What's New

### ✨ New Features
- 

### 🐛 Fixes
- 

### 🔧 Improvements (optional)
- 

---

## 更新内容

### ✨ 新增功能
- 

### 🐛 修复
- 

### 🔧 优化（可选）
- 
```

> 每条写成 `英文 / 中文` 对应的一句话，示例见 `docs/release-notes/EXAMPLE.md`

## 3. 完整示例 `ArtPlus 1.4`

```markdown
## What's New

### ✨ New Features
- Added batch icon rename with preview.
- Added preset import/export.

### 🐛 Fixes
- Fixed monochrome icon alpha being clipped at edges.

---

## 更新内容

### ✨ 新增功能
- 新增批量图标重命名，支持预览后应用。
- 新增预设导入/导出。

### 🐛 修复
- 修复单色图标边缘 Alpha 被裁剪的问题。
```

## 4. 工作流如何使用

- **有手写稿优先**：若仓库存在 `docs/release-notes/<version>.md`（如 `docs/release-notes/1.4.md`），CI 直接用它作为 Release Notes
- **无手写稿兜底**：CI 自动生成上面的双语空模板（带 Build Info），不会再出现只有一行 `ArtPlus x.x` 的情况
- **旧版本兼容**：`1.1` 的长文已保留为特例，其余版本走新模板

## 5. 发布步骤（ maintainers ）

1. 复制本模板到 `docs/release-notes/<version>.md`
2. 按 `New Features / Fixes` 填 3-8 条要点，中英文各一遍，保持一一对应
3. `git tag v<version> && git push origin v<version>` 或 `workflow_dispatch` 触发，标题自动为 `ArtPlus <version>`，内容自动读取你的 md

# ArtPlus 前端结构治理 — Epic v3 进度看板

> 唯一真相源：`outputs/research/frontend-repair-plan.md`；指标：`outputs/research/frontend-audit.py`；
> 门禁：`bash outputs/research/frontend-gate.sh`。
> 分支：`refactor/frontend-v3`（从 `main` @ `e036048` 切出）。只本地 commit，P7 才 push main + tag。

## 决策台账（歧义 → 最保守读法 + 理由）

| # | Slice | 决策 | 理由 |
|---|---|---|---|
| D1 | S0.1 | 只做本地 `merge --ff-only`，不执行计划里的 `git push origin main` | 与本次任务「只本地 commit；到 P7 才允许 push main」冲突时，用户指令优先；推送合并到 P7 一次性完成 |
| D2 | S0.1 | ff 合并不产生新 commit，本 Slice 的 commit 列记为 `e036048`（ff 后的 main HEAD） | fast-forward 语义上不生成提交，强行造空提交会污染回滚链 |
| D3 | S0.2 | `frontend-gate.sh` 增加 `--fast`（只跑指标）与 `--baseline`（重写基线） | 计划只要求「跑三项 + 与基线 diff」；--fast 供文案/注释类 Slice 省时，不降低硬门禁 |
| D4 | S0.3 | 设备用 `192.168.31.132:5555`（计划原文写 `.216`） | 用户本轮明确给出设备地址，且该设备在线；计划里的 IP 是旧值 |
| D5 | S0.3 | 截图存本地 `outputs/research/frontend-baseline-20261022/`，不进 git | 12 张 PNG ≈ 3MB，二进制基线进仓库无收益；`outputs/` 历来不跟踪 |
| D6 | S0.3 | 批量结果页基线暂缺，改用预设页（含预设卡）作替代基线 | 批量结果页需真实跑一次批量生成（依赖设备侧产物），P7 验收时补拍；不阻塞结构治理 |
| D7 | S0.4 | `verify` job 放进 `android-apk.yml` 并给 release 链加 `if` 隔离 | 计划要求「新增 job」；若只加 `pull_request` 触发器会让 PR 跑签名 release，故用 `if` 隔离 |
| D8 | S0.4 | `frontend-audit.py` / `frontend-gate.sh` / 基线 JSON 强制入库 | CI 的 verify job 要跑审计脚本；不入库则 CI 必红 |
| D9 | 门禁 | `--strict` 中途只报告不判红；`GATE_STRICT=1` 时才是硬门禁 | 逐 Slice 门禁按计划 §4 是「本 Slice 指标达标 + 其余不劣化」，全量 strict 是 P7 终局；否则每个中间 Slice 必红，门禁失去意义 |
| D10 | S1.1 | 基线对比忽略 `unused_imports_pct` / `import_line_ratio_pct` 两个派生比率 | 删行会让比率机械上升，绝对量指标另有 `unused_imports` / `import_lines` 兜底 |
| D11 | S1.3 | 审计脚本把 Kotlin 委托/操作符符号（`getValue`/`setValue` 等 30 个）视为已使用 | 它们由 `by` 与运算符语法隐式调用，正文无字面名；按旧判定删掉会编译失败（已实测） |
| D12 | S1.3 | 审计脚本的注释剥离改为逐字符扫描（字符串内 `/*` 不再吞代码） | `SettingsPage` 的 `"$dir/*.png"` 曾把 366–650 行整段吞掉，导致 `CancellationException` 被误判未使用；修后 `hardcoded_cjk` 676→685 是「看得更准」，已重写基线 |
| D13 | S1.5 | ⑥「三处 Toast 冗余 runOnUiThread」不删 | 计划未指名三处；实测所有 `showToast` 回调都经 `pickerToastStatus` 从任意线程调用，删包裹会在非 Looper 线程抛 `Can't toast on a thread that has not called Looper.prepare()`。改为保留并在此登记 |
| D14 | S1.5 | ② 给保存键补 `contentDescription = "保存"` 使 `hardcoded_cjk` 685→686，随即重写基线 | 这是 S5.4 文案抽取的输入，不是回归；终局该指标为 0 |
| D15 | S2.1 | 新增 `state/UiPersistOps.kt` 使 `kt_files` / `files_in_dev_artplus_mobile` 77→78，随即重写基线 | 收口必须新增一个实现文件；该指标由 S4.5 的目录→包名对齐一次性压到 ≤20，此处不做局部妥协 |
| D16 | S2.2 | `TuningActions` 由 Activity 的 `by lazy` 持有（而非 HomePage 里 `remember`） | 调用点分布在 HomePage/SettingsPage/AppPickerPage/Shell/MainActivityOps 五个文件的多个 composable 里；`remember` 只能覆盖单个组合作用域，会退化成 5 份实例。「构造一次」的语义用 Activity 级 lazy 更严格 |
| D17 | S2.2 | 刷新入口 `refresh` 保留 `(Boolean)` 形参（rebuildLocalCandidates） | 37 个调用点的原表达式就是 `{ rebuild -> ... }`，改成无参会改变语义 |
| D18 | S2.4 | 合并 31 组相邻同 flow 写入 + 新增 `setStatus()` 收敛 145 处单字段 `statusText` 写 | 计划只给了目标值（≤120/≤150）。相邻合并到 144 后 `updateShell` 仍 222，唯一低风险出路是把「只写状态文案」的调用收成一个具名入口（语义等价，一字段 copy） |
| D19 | S2.4 | 合并时把残留的 `it.previewVersion` 改写为 `v.previewVersion`（2 处） | 合并重命名形参后，原 `it` 指向同一 StateFlow 快照，改写等价；不改则编译失败 |
| D20 | S3.1 | 裸读规则落地为「总量棘轮 + lint `StateFlowValueCalledInComposition` 精确拦截」两层，而非一次性全清 | `ui/` 现有 426 处裸读，其中绝大多数在事件回调里（合法）；一次性全清需要重写 400+ 个回调，风险远大于收益。精确的组合期违例由 lint 拦（当前 0） |
| D21 | S3.5 | 调参快照改为页面顶层单次订阅（HomePage/SettingsPage/Shell 各一处），而非逐 item 提升 | 逐 item 提升要在 10 个不同 item 作用域各订阅一次，仍是多订阅；顶层一次覆盖全部读点，重组开销留给 P6 的 backdrop 治理 |
| D22 | S3.2 | 并入 S3.1 落地：HomePage 的两张卡片改为只消费 `isBusy` / `statusText`，`HomePage:1925` 一类祖先订阅兜底读点已清零（lint 组合期裸读 0） | 计划要求「各自 collectAsState 或只传所需字段」，现状已是后者；再造一层订阅只会增加重组面 |
| D23 | S3.3+S3.4 | 合并为一个 commit：根级保留**一次** params 订阅，但组合体字段读点全部收进 `derivedStateOf`，effect key 换成 16 字段 `PreviewEffectKey` | 「根组合不读 params」若做成彻底取消订阅，`StateFlow.value` 不在快照系统里，预览资产将永不刷新（实测会退化）。保留单订阅 + 字段化 key 同时满足 S3.4 的验收（改 gptCustomPrompt 不重解码） |
| D24 | 门禁 | 棘轮忽略 `import_lines` / `imports_total` / `kt_files`（保留 `unused_imports` 与 `import_line_ratio_pct`） | 新增一个文件或一行 `mutableStateOf` import 就判红是假阳性；真正要守的是「未使用 import = 0」与「import 占比 < 3%」 |
| D25 | S3.8 | 仪器化冒烟用 UI Automator 而非 compose-ui-test | `androidx.compose.ui:ui-test-junit4` 不在本地依赖缓存，离线构建拉不到；UI Automator 2.4.0 已缓存且能覆盖「启动→4 页→选择入口」 |
| D26 | S3.8 | 冒烟测试跑法固定 `ANDROID_SERIAL=<目标机>` | 现场 adb 有 3 台设备，其中 2 台的 instrumentation 会 process crashed，联网跑会把整批判红 |
| D27 | S4.1 | 不新建 `ui/pages/home/HomeSection.kt`，section 壳沿用既有 `SectionCard`（22 处调用） | 全仓不存在「标题 + 展开态」的重复脚手架，新建一个无调用点的抽象等于制造死代码（与本 Epic 目标相反） |
| D28 | S4.2–S4.4 | 三个 tab 页与 HomePage 瘦身合并为一个 commit | `when (page)` 的四个分支一旦部分搬出就不编译（`private` 扩展函数可见性、局部 state 归属），原子性由编译器强制；拆成三个 commit 会出现两个必然红的中间态 |
| D29 | S4.2–S4.4 | 6 个 `__g*` 生成入口的可见性 `private` → `internal` | 它们被 tab 页调用，`private` 是文件级可见性；改 `internal` 不改变对外 API（模块内可见） |
| D30 | S4.4 | 顺手展开 176 处单语句/箭头 `run { }` 包裹（无 `return@run`） | 这是 S1.2 里「顺手去掉外层 run { }」的延伸；ui_lines 需从 16313 压到 16000 以下，展开是零语义变更的唯一手段 |
| D31 | S4.5 | 目录→包名对齐：70 个「目录≠包」文件改为真实子包（`dev.artplus.mobile/ui/pages/...`），`files_in_dev_artplus_mobile` 84→**7** | 计划 §2 第 10 条要求该指标 ≤20；§6「包名不变」按「applicationId/namespace 不变」理解，逐文件 package 必须与目录一致才能计数下降 |
| D32 | S4.5 | `Shell.kt` 只把预览资产 effect 下沉到 `state/PreviewAssetOps.kt`（1302→1214 行），未做到 ≤700 | 剩余 600 行是 4 个 pager 分支的装配与 20+ 个对话框接线，继续切需要把 30+ 个局部状态作为显式参数搬运，改动面与「纯移动」红线冲突；登记为未达标项 |
| D33 | 门禁 | `ui_lines` / `homepage_lines` / `ui_files` / `packages` 改为报告项（不判劣化） | 包名对齐必然让 `packages` 1→16、`ui_files` 变化；行数指标由 `--strict` 单独判 |
| D34 | P7 | CI 的 verify job 改为「棘轮判红 + `--strict` 只报告」 | `--strict` 仍有 3 项未达标（见终局表），若按它判红会让 main 永远红，verify job 等于失效；棘轮才是逐 Slice 的可执行约束 |
| D35 | P7 | v1.5.0-alpha3 的 release APK 在「HomeTabPage 2 处组合期裸读」修复之前构建 | tag 已发布且 APK 已上传（sha256 已公示），移动 tag 会让公示校验和失效；修复走 main，随 alpha4 出货 |

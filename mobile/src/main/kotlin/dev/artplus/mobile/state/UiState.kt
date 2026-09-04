package dev.artplus.mobile

import android.net.Uri

/**
 * Slice 3.1 状态收敛：非调参 UI 状态的结构化分组。
 *
 * - 调参子集已收敛为 MainViewModel.params（单一 MutableStateFlow<TuningParams>），本文件不重复。
 * - draft*Text 草稿态留 UI 层（MainActivity），不进 ViewModel。
 * - 以下 10 组覆盖 MainActivity 原 102 处 `by mutableStateOf` + 4 处 `by mutableIntStateOf`
 *  （131 - 29 draft = 102，+4 Int = 106 迁入项），默认值与 MainActivity 基线逐字一致，
 *   只改持有位置与读写路径，不改默认值、读写顺序与持久化语义。
 */

internal data class PickerState(
    val queryText: String = "",
    val selectedPackageName: String? = null,
    val showSystemApps: Boolean = false,
    val generatedFilter: GeneratedFilter = GeneratedFilter.All,
    val generatedPackageNames: Set<String> = emptySet(),
    val multiSelectedPackageNames: Set<String> = emptySet(),
    val packageListPermissionGranted: Boolean = true,
    val usageAccessGranted: Boolean = false,
    val isScanningGeneratedPackages: Boolean = false,
    val generatedScanFailed: Boolean = false,
)

internal data class ShellState(
    val currentPage: AppPage = AppPage.Home,
    val statusText: String = "加载应用列表中...",
    val isBusy: Boolean = false,
    val outputTreeUri: Uri? = null,
    val advancedSettingsCategory: AdvancedSettingsCategory = AdvancedSettingsCategory.LiquidGlass,
    val advancedSettingsTab: AdvancedSettingsTab = AdvancedSettingsTab.Sliders,
    val onboardingVisible: Boolean = false,
)

internal data class GptRmbgSettingsState(
    val gptModelId: String = "",
    val gptBaseUrl: String = "",
    val gptApiKey: String = "",
    val gptSettingsSaveStatus: String = "",
    val rmbgComponentUrl: String = "",
    val rmbgComponentSaveStatus: String = "",
    val rmbgComponentStatus: String = "",
)

internal data class GlassBarState(
    val liquidGlassBottomBarEnabled: Boolean = true,
    val liquidGlassBottomBarBlurEnabled: Boolean = true,
)

internal data class PresetUiState(
    val activePresetId: String? = null,
    val activePresetBaseParams: TuningParams? = null,
    val presetListVersion: Int = 0,
    val batchOutputMode: BatchOutputMode = BatchOutputMode.Root,
    val gptRunCount: Int = 0,
    val rmbgRunCount: Int = 0,
    val presetSaveDialogVisible: Boolean = false,
    val presetSaveName: String = "",
    val presetImportDialogVisible: Boolean = false,
    val presetImportText: String = "",
    val presetRenameTarget: TuningPreset? = null,
    val presetActionMenuTarget: TuningPreset? = null,
    val presetDeleteConfirmTarget: TuningPreset? = null,
    val presetSearchQuery: String = "",
    val presetListExpanded: Boolean = false,
    val presetBatchPreviewConfirmTarget: TuningPreset? = null,
    val activeBatchPreviewPreset: TuningPreset? = null,
    val showBatchPreviewRefreshConfirm: Boolean = false,
    val batchPreviewProgress: BatchPreviewProgress? = null,
    val batchPreviewResult: BatchPreviewResult? = null,
    val batchPreviewCancelled: Boolean = false,
    val isGeneratingBatchPreview: Boolean = false,
)

internal data class BatchPreviewConfigState(
    val batchPreviewCount: Int = BatchPreviewSampler.DEFAULT_BATCH_PREVIEW_COUNT,
    val batchPreviewColumns: Int = 4,
    val batchPreviewIconSizeDp: Int = 54,
    val batchPreviewCornerRadiusDp: Int = 20,
    val batchPreviewDesktopBackground: PreviewDesktopBackground = PreviewDesktopBackground.DarkGray,
    val customWallpaperPath: String? = null,
    val customWallpaperInfo: String = "",
)

internal data class ConfirmUiState(
    val pendingServiceConfirm: ServiceConfirmRequest? = null,
    val autoConfirmRootWrite: Boolean = false,
    val pendingRootWriteConfirm: RootWriteConfirmRequest? = null,
    val rootWriteConfirmRememberSkip: Boolean = false,
    val refreshConfirmVisible: Boolean = false,
    val autoConfirmRefresh: Boolean = false,
    val refreshConfirmRememberAuto: Boolean = false,
)

internal data class TransferState(
    val batchApplyProgress: BatchApplyProgress? = null,
    val exportProgress: ExportProgress? = null,
    val backupProgress: ExportProgress? = null,
    val backupSheetVisible: Boolean = false,
    val singleExportSheetVisible: Boolean = false,
    val backupInBackground: Boolean = false,
    val backupBackgroundDots: Int = 1,
)

internal data class PreviewSessionState(
    val previewPackageName: String? = null,
    val previewDirPath: String? = null,
    val previewVersion: Int = 0,
    val previewStripEnabled: Boolean = false,
    val sharedPreviewAssets: PreviewAssets? = null,
    val activeGenerationSession: GenerationSession? = null,
    val previewDesktopBackground: PreviewDesktopBackground = PreviewDesktopBackground.DarkGray,
    val previewCornerRadiusDp: Int = DEFAULT_PREVIEW_CORNER_RADIUS_DP,
    val previewIconSizeDp: Int = DEFAULT_PREVIEW_ICON_SIZE_DP,
    val previewChoiceMode: PreviewMode? = null,
    val isGptPreviewLoading: Boolean = false,
    val isGeneratingGptCandidate: Boolean = false,
    val isGeneratingRmbgCandidate: Boolean = false,
    val isRefreshingArtPlusIcons: Boolean = false,
    val isPreviewAssetsRefreshing: Boolean = false,
    val isPreviewOutputRefreshing: Boolean = false,
    val lastRmbgCandidateError: String? = null,
    val rmbgCandidatePackageName: String? = null,
    val rmbgCandidateMode: PreviewMode? = null,
    val rmbgCandidateStatusText: String = "",
    val rmbgCandidateFailurePackageName: String? = null,
    val rmbgCandidateFailureMode: PreviewMode? = null,
    val skipNextHomeReturnAnimation: Boolean = false,
    val pendingCustomImageMode: PreviewMode? = null,
    val pendingCustomImageKind: CustomImageKind? = null,
    val isInstallingRmbgComponent: Boolean = false,
    val rmbgInstallStage: String = "",
    val rmbgInstallProgress: Float? = null,
    val rmbgDialogVisible: Boolean = false,
    val exportDialogVisible: Boolean = false,
    val resetDefaultsDialogVisible: Boolean = false,
    val lastRmbgInferenceReport: RmbgInferenceReport? = null,
    val lastParamsSnapshot: TuningParams? = null,
)

internal data class UpdateUiState(
    val isCheckingUpdate: Boolean = false,
    val updateAvailableInfo: UpdateInfo? = null,
    val updateUpToDateDialogVisible: Boolean = false,
    val mitLicenseDialogVisible: Boolean = false,
)

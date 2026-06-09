const uploadForm = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const fileName = document.getElementById("fileName");
const uploadButton = document.getElementById("uploadButton");
const serverStatus = document.getElementById("serverStatus");
const jobStatus = document.getElementById("jobStatus");
const packageName = document.getElementById("packageName");
const previewGrid = document.getElementById("previewGrid");
const logOutput = document.getElementById("logOutput");
const clearLogs = document.getElementById("clearLogs");
const downloadLocal = document.getElementById("downloadLocal");
const downloadGpt = document.getElementById("downloadGpt");
const historyList = document.getElementById("historyList");
const refreshHistory = document.getElementById("refreshHistory");

let currentJob = null;
let eventSource = null;
let historyItems = [];
const iconCache = new Map();
const currentJobStorageKey = "artplus.currentJobId";

const variantOrder = ["local", "gpt"];
const variantLabels = {
  local: "本地版",
  gpt: "GPT版",
};

const statusLabels = {
  pending: "Pending",
  queued: "Queued",
  running: "Running",
  succeeded: "Succeeded",
  failed: "Failed",
  partial: "Partial",
};

function setStatus(status) {
  jobStatus.textContent = statusLabels[status] || status || "Idle";
  jobStatus.className = `status-pill ${status || ""}`;
}

function appendLog(event) {
  const prefix = event.variant ? `[${variantLabels[event.variant] || event.variant}]` : "[系统]";
  const line = `${event.ts || ""} ${prefix} ${event.message || ""}`;
  logOutput.textContent += `${line}\n`;
  logOutput.scrollTop = logOutput.scrollHeight;
}

function setDownload(anchor, url) {
  if (url) {
    anchor.href = url;
    anchor.classList.remove("disabled");
  } else {
    anchor.href = "#";
    anchor.classList.add("disabled");
  }
}

function formatHistoryTime(value) {
  if (!value) {
    return "";
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString([], {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function setActiveHistory(jobId) {
  historyList.querySelectorAll(".history-item").forEach((item) => {
    item.classList.toggle("active", item.dataset.jobId === jobId);
  });
}

function renderHistory(items) {
  historyItems = items || [];
  historyList.replaceChildren();
  if (!historyItems.length) {
    const empty = document.createElement("div");
    empty.className = "history-empty";
    empty.textContent = "暂无历史";
    historyList.append(empty);
    return;
  }

  historyItems.forEach((item) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "history-item";
    button.dataset.jobId = item.id;

    const text = document.createElement("div");
    text.className = "history-text";

    const title = document.createElement("div");
    title.className = "history-title";
    title.textContent = item.package_name || item.original_filename || item.id;

    const meta = document.createElement("div");
    meta.className = "history-meta";
    meta.textContent = `${item.original_filename || ""} · ${formatHistoryTime(item.updated_at)}`;

    const badge = document.createElement("div");
    badge.className = `history-badge ${item.status || ""}`;
    badge.textContent = statusLabels[item.status] || item.status || "Pending";

    text.append(title, meta);
    button.append(text, badge);
    button.addEventListener("click", () => loadJob(item.id, { clearLogs: true }));
    historyList.append(button);
  });

  if (currentJob) {
    setActiveHistory(currentJob.id);
  }
}

async function loadHistory(selectLatest = false) {
  const response = await fetch("/api/jobs?limit=50");
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(payload.detail || "加载历史失败");
  }
  const items = payload.items || [];
  renderHistory(items);
  if (selectLatest && !currentJob && items.length) {
    const rememberedJobId = localStorage.getItem(currentJobStorageKey);
    const selected = items.find((item) => item.id === rememberedJobId) || items[0];
    await loadJob(selected.id, { clearLogs: true });
  }
}

async function loadJob(jobId, options = {}) {
  const response = await fetch(`/api/jobs/${jobId}`);
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(payload.detail || "加载任务失败");
  }
  if (options.clearLogs) {
    logOutput.textContent = "";
  }
  renderJob(payload);
  connectEvents(payload.id);
}

function loadImage(url) {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.crossOrigin = "same-origin";
    image.onload = () => resolve(image);
    image.onerror = () => reject(new Error(`图片加载失败: ${url}`));
    image.src = `${url}${url.includes("?") ? "&" : "?"}t=${Date.now()}`;
  });
}

async function composeIcon(variant, mode, originalIconUrl) {
  const assets = variant.assets || {};
  const fgAsset = assets["recfg.png"];
  const nightAsset = assets["rec_night.png"];
  const cacheKey = `${mode}:${originalIconUrl || ""}:${fgAsset || ""}:${nightAsset || ""}`;
  if (iconCache.has(cacheKey)) {
    return iconCache.get(cacheKey);
  }

  const canvas = document.createElement("canvas");
  canvas.width = 240;
  canvas.height = 240;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, 240, 240);

  if (mode === "light") {
    if (!originalIconUrl) {
      throw new Error("缺少原始 App 图标背景");
    }
    if (!fgAsset) {
      throw new Error("缺少 recfg.png");
    }
    const [originalIcon, fg] = await Promise.all([loadImage(originalIconUrl), loadImage(fgAsset)]);
    ctx.drawImage(originalIcon, 0, 0, 240, 240);
    ctx.drawImage(fg, 0, 0, 240, 240);
  } else {
    if (!nightAsset) {
      throw new Error("缺少 rec_night.png");
    }
    const night = await loadImage(nightAsset);
    const gradient = ctx.createLinearGradient(0, 0, 240, 240);
    gradient.addColorStop(0, "#252f3f");
    gradient.addColorStop(1, "#101827");
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, 240, 240);
    ctx.drawImage(night, 0, 0, 240, 240);
  }

  const dataUrl = canvas.toDataURL("image/png");
  iconCache.set(cacheKey, dataUrl);
  return dataUrl;
}

function makePlaceholder(label) {
  const slot = document.createElement("div");
  slot.className = "app-slot";
  const icon = document.createElement("div");
  icon.className = "placeholder-icon";
  const text = document.createElement("div");
  text.className = "app-label";
  text.textContent = label;
  slot.append(icon, text);
  return slot;
}

function makeRenderedSlot(iconUrl, label) {
  const slot = document.createElement("div");
  slot.className = "app-slot";
  const image = document.createElement("img");
  image.className = "rendered-icon";
  image.alt = `${label} icon`;
  image.src = iconUrl;
  const text = document.createElement("div");
  text.className = "app-label";
  text.textContent = label;
  slot.append(image, text);
  return slot;
}

function buildDevice(mode, iconUrl, label) {
  const device = document.createElement("div");
  device.className = `device ${mode}`;

  const desktop = document.createElement("div");
  desktop.className = "desktop";

  const statusbar = document.createElement("div");
  statusbar.className = "statusbar";
  statusbar.innerHTML = "<span>10:08</span><span>5G 87%</span>";

  const grid = document.createElement("div");
  grid.className = "app-grid";
  const names = ["相册", "天气", "音乐", "日历", "文件", label, "浏览器", "设置", "便签", "钱包", "地图", "应用"];
  names.forEach((name, index) => {
    grid.append(index === 5 ? makeRenderedSlot(iconUrl, label) : makePlaceholder(name));
  });

  const dock = document.createElement("div");
  dock.className = "dock";
  ["电话", "短信", "相机", "商店"].forEach((name) => dock.append(makePlaceholder(name)));

  desktop.append(statusbar, grid, dock);
  device.append(desktop);
  return device;
}

function errorPanel(variantKey, message, canRetry) {
  const panel = document.createElement("div");
  panel.className = "variant-error";
  const title = document.createElement("strong");
  title.textContent = "无法预览";
  const text = document.createElement("p");
  text.textContent = message || "处理未完成";
  panel.append(title, text);
  if (canRetry) {
    const button = document.createElement("button");
    button.className = "retry-button";
    button.type = "button";
    button.textContent = "重试";
    button.addEventListener("click", () => retryVariant(variantKey));
    panel.append(button);
  }
  return panel;
}

function makeCell(title) {
  const cell = document.createElement("div");
  cell.className = "cell";
  cell.dataset.title = title;
  return cell;
}

async function fillPreviewCell(cell, variantKey, variant, mode) {
  if (!variant || variant.status !== "succeeded") {
    const canRetry = variant && variant.status === "failed";
    cell.append(errorPanel(variantKey, variant?.error || "等待处理完成", canRetry));
    return;
  }
  try {
    const dataUrl = await composeIcon(variant, mode, currentJob?.preview_assets?.original_icon);
    const label = currentJob?.package_name || "App";
    cell.replaceChildren(buildDevice(mode, dataUrl, label));
  } catch (error) {
    cell.replaceChildren(errorPanel(variantKey, error.message, false));
  }
}

function renderPreview(job) {
  previewGrid.replaceChildren();

  const corner = document.createElement("div");
  corner.className = "cell header-cell";
  previewGrid.append(corner);

  variantOrder.forEach((key) => {
    const header = document.createElement("div");
    header.className = "cell header-cell";
    header.textContent = variantLabels[key];
    previewGrid.append(header);
  });

  [
    ["light", "亮色"],
    ["dark", "暗色"],
  ].forEach(([mode, label]) => {
    const rowLabel = document.createElement("div");
    rowLabel.className = "cell row-label";
    rowLabel.textContent = label;
    previewGrid.append(rowLabel);

    variantOrder.forEach((key) => {
      const cell = makeCell(`${variantLabels[key]} / ${label}`);
      previewGrid.append(cell);
      fillPreviewCell(cell, key, job.variants[key], mode);
    });
  });
}

function renderJob(job) {
  currentJob = job;
  setStatus(job.status);
  packageName.textContent = job.package_name || "未选择任务";
  serverStatus.textContent = job.original_filename
    ? `${job.original_filename} · ${job.id}`
    : "等待上传";
  setDownload(downloadLocal, job.variants?.local?.download_url);
  setDownload(downloadGpt, job.variants?.gpt?.download_url);
  renderPreview(job);
  setActiveHistory(job.id);
  localStorage.setItem(currentJobStorageKey, job.id);
}

function connectEvents(jobId) {
  if (eventSource) {
    eventSource.close();
  }
  eventSource = new EventSource(`/api/jobs/${jobId}/events`);
  eventSource.addEventListener("log", (message) => {
    try {
      appendLog(JSON.parse(message.data));
    } catch {
      logOutput.textContent += `${message.data}\n`;
    }
  });
  eventSource.addEventListener("snapshot", (message) => {
    const job = JSON.parse(message.data);
    const previousState = [
      currentJob?.status,
      currentJob?.variants?.local?.status,
      currentJob?.variants?.gpt?.status,
    ].join(":");
    const nextState = [
      job.status,
      job.variants?.local?.status,
      job.variants?.gpt?.status,
    ].join(":");
    renderJob(job);
    if (previousState !== nextState) {
      loadHistory(false).catch(() => {});
    }
  });
  eventSource.onerror = () => {
    if (currentJob && ["succeeded", "failed", "partial"].includes(currentJob.status)) {
      eventSource.close();
    }
  };
}

async function uploadSelectedFile() {
  const file = fileInput.files?.[0];
  if (!file) {
    serverStatus.textContent = "请选择 ZIP 或 APK 文件";
    return;
  }
  uploadButton.disabled = true;
  setStatus("running");
  serverStatus.textContent = "正在上传";
  logOutput.textContent = "";
  previewGrid.innerHTML = '<div class="empty-state">上传中</div>';
  const formData = new FormData();
  formData.append("file", file);
  try {
    const response = await fetch("/api/jobs", {
      method: "POST",
      body: formData,
    });
    const payload = await response.json();
    if (!response.ok) {
      throw new Error(payload.detail || "上传失败");
    }
    appendLog({ ts: new Date().toLocaleTimeString(), message: `任务已创建: ${payload.id}` });
    renderJob(payload);
    await loadHistory(false);
    connectEvents(payload.id);
  } catch (error) {
    setStatus("failed");
    serverStatus.textContent = error.message;
    appendLog({ ts: new Date().toLocaleTimeString(), level: "error", message: error.message });
  } finally {
    uploadButton.disabled = false;
  }
}

async function retryVariant(variant) {
  if (!currentJob) {
    return;
  }
  try {
    const response = await fetch(`/api/jobs/${currentJob.id}/retry/${variant}`, {
      method: "POST",
    });
    const payload = await response.json();
    if (!response.ok) {
      throw new Error(payload.detail || "重试失败");
    }
    appendLog({ ts: new Date().toLocaleTimeString(), variant, message: "已提交重试" });
    renderJob(payload);
    await loadHistory(false);
    connectEvents(payload.id);
  } catch (error) {
    appendLog({ ts: new Date().toLocaleTimeString(), variant, level: "error", message: error.message });
  }
}

uploadForm.addEventListener("submit", (event) => {
  event.preventDefault();
  uploadSelectedFile();
});

fileInput.addEventListener("change", () => {
  const file = fileInput.files?.[0];
  fileName.textContent = file ? file.name : "拖入文件或点击选择";
});

uploadForm.addEventListener("click", (event) => {
  if (event.target === uploadButton) {
    return;
  }
  fileInput.click();
});

["dragenter", "dragover"].forEach((name) => {
  uploadForm.addEventListener(name, (event) => {
    event.preventDefault();
    uploadForm.classList.add("dragging");
  });
});

["dragleave", "drop"].forEach((name) => {
  uploadForm.addEventListener(name, (event) => {
    event.preventDefault();
    uploadForm.classList.remove("dragging");
  });
});

uploadForm.addEventListener("drop", (event) => {
  const file = event.dataTransfer?.files?.[0];
  if (!file) {
    return;
  }
  const dataTransfer = new DataTransfer();
  dataTransfer.items.add(file);
  fileInput.files = dataTransfer.files;
  fileName.textContent = file.name;
});

clearLogs.addEventListener("click", () => {
  logOutput.textContent = "";
});

refreshHistory.addEventListener("click", () => {
  loadHistory(false).catch((error) => {
    appendLog({ ts: new Date().toLocaleTimeString(), level: "error", message: error.message });
  });
});

setStatus("");
loadHistory(true).catch((error) => {
  appendLog({ ts: new Date().toLocaleTimeString(), level: "error", message: error.message });
});

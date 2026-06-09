# ArtPlus

ArtPlus manages ColorOS ART+ icon assets and produces app-specific icon packages that can be placed under the OPPO uxicons directory.

## Language

**Processing Job**:
A single WebUI upload session that owns the uploaded ZIP, logs, variant outputs, and downloadable results.
_Avoid_: Task, run

**Variant**:
One generated version of a Processing Job output, produced with a specific image-generation strategy.
_Avoid_: Mode, version

**本地版**:
The Variant generated without GPT Image 2, using only local pipeline behavior.
_Avoid_: local-only, 未生图

**GPT版**:
The Variant generated with GPT Image 2 and considered successful only when GPT Image 2 actually produced the core icon layers.
_Avoid_: gpt-image-2, 生图版

**GPT Image Backend**:
The implementation used by GPT版 to produce core image layers. `service` uses the local 9714 queue service; `direct` calls an OpenAI-compatible `/v1/images/edits` endpoint with URL + API key.
_Avoid_: hidden upstream, hard-coded 9714

**Device Preview**:
A browser-rendered phone desktop simulation that shows a generated icon with rounded corners in light and dark appearances.
_Avoid_: Screenshot, real device render

**Original Icon Background**:
The uploaded app's original icon image used as the light Device Preview base layer, so the light preview keeps the app's own background instead of the generated `recbg.png`.
_Avoid_: recbg, generated background

**Foreground Subject Scale**:
The visible alpha-bounded subject in `recfg.png` should occupy about 70% of the square icon side in generated outputs and previews.
_Avoid_: raw extraction size, model default scale

**Generation History**:
A SQLite-backed index of Processing Jobs that lets the WebUI restore previous results, previews, logs, and download links after page refreshes or later visits.
_Avoid_: in-memory selected job, transient UI state

**APK Icon ZIP**:
A fast-extracted ZIP made from one APK by copying only launcher-icon resources, `AndroidManifest.xml`, and `resources.arsc`, plus a top-level `icon.*` copied from the selected launcher image for the existing pipeline.
_Avoid_: decompiled APK, full resource dump

**Monochrome Alpha Mask**:
The ART+ `monochrome*.png` output where RGB is fixed white and the foreground luminance is encoded into alpha, so ColorOS/Material themed icons preserve tonal contrast when the system applies a single color.
_Avoid_: flat single-color monochrome, grayscale RGB icon

package dev.artplus.mobile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int SIZE_1X1 = 240;
    private static final int SIZE_2X2 = 704;
    private static final int[] SIZE_1X2 = {240, 820};
    private static final int[] SIZE_2X1 = {820, 240};
    private static final int REQUEST_TREE = 1001;
    private static final int MONO_ALPHA_MIN = 40;
    private static final int MONO_ALPHA_MAX = 230;
    private static final double MONO_ALPHA_GAMMA = 0.85;

    private final List<AppEntry> apps = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private AppEntry selectedApp;
    private Uri outputTreeUri;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ArtPlus Mobile");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("ArtPlus Mobile");
        title.setTextSize(22);
        title.setTextColor(Color.rgb(20, 24, 31));
        root.addView(title);

        statusView = new TextView(this);
        statusView.setText("加载应用列表中...");
        statusView.setPadding(0, 12, 0, 12);
        root.addView(statusView);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button chooseDir = new Button(this);
        chooseDir.setText("选择输出目录");
        chooseDir.setOnClickListener(v -> chooseOutputDirectory());
        buttons.addView(chooseDir, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button generate = new Button(this);
        generate.setText("生成本地版");
        generate.setOnClickListener(v -> generateSelected(false));
        buttons.addView(generate, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button rootInstall = new Button(this);
        rootInstall.setText("Root写入");
        rootInstall.setOnClickListener(v -> generateSelected(true));
        buttons.addView(rootInstall, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(buttons);

        ListView listView = new ListView(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedApp = apps.get(position);
            status("已选择: " + selectedApp.label + " (" + selectedApp.packageName + ")");
        });
        root.addView(listView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
        loadApps();
    }

    private void chooseOutputDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_TREE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TREE && resultCode == RESULT_OK && data != null) {
            outputTreeUri = data.getData();
            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (outputTreeUri != null) {
                getContentResolver().takePersistableUriPermission(outputTreeUri, flags);
                status("已选择输出目录");
            }
        }
    }

    private void loadApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
            Map<String, AppEntry> unique = new LinkedHashMap<>();
            for (ResolveInfo info : resolveInfos) {
                if (info.activityInfo == null) {
                    continue;
                }
                String packageName = info.activityInfo.packageName;
                CharSequence labelSeq = info.loadLabel(pm);
                String label = labelSeq == null ? packageName : labelSeq.toString();
                unique.put(packageName, new AppEntry(label, packageName, info));
            }
            apps.clear();
            apps.addAll(unique.values());
            apps.sort(Comparator.comparing(entry -> entry.label.toLowerCase(Locale.ROOT)));

            List<String> labels = new ArrayList<>();
            for (AppEntry entry : apps) {
                labels.add(entry.label + "\n" + entry.packageName);
            }
            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(labels);
                adapter.notifyDataSetChanged();
                status("共 " + apps.size() + " 个启动器应用。选择一个后生成。");
            });
        }).start();
    }

    private void generateSelected(boolean installWithRoot) {
        if (selectedApp == null) {
            status("先选择一个应用");
            return;
        }
        status("处理中: " + selectedApp.packageName);
        new Thread(() -> {
            try {
                File outDir = generateArtPlusPackage(selectedApp);
                if (outputTreeUri != null) {
                    exportToTree(outDir);
                }
                if (installWithRoot) {
                    installWithRoot(outDir, selectedApp.packageName);
                    status("已生成并尝试 Root 写入: " + selectedApp.packageName);
                } else {
                    status("已生成: " + outDir.getAbsolutePath());
                }
            } catch (Exception e) {
                status("失败: " + e.getMessage());
            }
        }).start();
    }

    private File generateArtPlusPackage(AppEntry app) throws Exception {
        File base = getExternalFilesDir("ArtPlus");
        if (base == null) {
            base = new File(getFilesDir(), "ArtPlus");
        }
        File outDir = new File(base, app.packageName);
        ensureCleanDir(outDir);

        Drawable icon = app.resolveInfo.loadIcon(getPackageManager());
        Bitmap recfg;
        Bitmap recbg;
        if (icon instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) icon;
            Drawable foreground = adaptive.getForeground();
            Drawable background = adaptive.getBackground();
            recfg = drawDrawable(foreground, SIZE_1X1, SIZE_1X1, true);
            recbg = drawDrawable(background == null ? new ColorDrawable(Color.WHITE) : background, SIZE_1X1, SIZE_1X1, false);
        } else {
            recfg = drawDrawable(icon, SIZE_1X1, SIZE_1X1, true);
            recbg = solidBitmap(SIZE_1X1, SIZE_1X1, Color.WHITE);
        }

        savePng(recbg, new File(outDir, "recbg.png"));
        savePng(recfg, new File(outDir, "recfg.png"));
        savePng(resizeBitmap(recbg, SIZE_1X2[0], SIZE_1X2[1]), new File(outDir, "recbg_1x2.png"));
        savePng(resizeBitmap(recbg, SIZE_2X1[0], SIZE_2X1[1]), new File(outDir, "recbg_2x1.png"));
        savePng(resizeBitmap(recbg, SIZE_2X2, SIZE_2X2), new File(outDir, "recbg_2x2.png"));

        Bitmap recfg1x2 = centerOnCanvas(recfg, SIZE_1X2[0], SIZE_1X2[1]);
        Bitmap recfg2x1 = centerOnCanvas(recfg, SIZE_2X1[0], SIZE_2X1[1]);
        Bitmap recfg2x2 = centerOnCanvas(recfg, SIZE_2X2, SIZE_2X2);
        savePng(recfg1x2, new File(outDir, "recfg_1x2.png"));
        savePng(recfg2x1, new File(outDir, "recfg_2x1.png"));
        savePng(recfg2x2, new File(outDir, "recfg_2x2.png"));

        int bgColor = sampleColor(recbg);
        savePng(recolorAlpha(recfg, bgColor), new File(outDir, "rec_night.png"));
        savePng(recolorAlpha(recfg1x2, bgColor), new File(outDir, "rec_night_1x2.png"));
        savePng(recolorAlpha(recfg2x1, bgColor), new File(outDir, "rec_night_2x1.png"));
        savePng(recolorAlpha(recfg2x2, bgColor), new File(outDir, "rec_night_2x2.png"));

        savePng(monochromeAlpha(recfg), new File(outDir, "monochrome.png"));
        savePng(monochromeAlpha(recfg1x2), new File(outDir, "monochrome_1x2.png"));
        savePng(monochromeAlpha(recfg2x1), new File(outDir, "monochrome_2x1.png"));
        savePng(monochromeAlpha(recfg2x2), new File(outDir, "monochrome_2x2.png"));

        savePng(adjustColor(recfg, 1.3f, 1.0f), new File(outDir, "day.png"));
        savePng(adjustColor(recfg, 0.9f, 0.9f), new File(outDir, "nsd.png"));
        savePng(adjustColor(recfg, 0.9f, 1.05f), new File(outDir, "mat.png"));
        savePng(adjustColor(recfg, 0.7f, 0.95f), new File(outDir, "peb.png"));
        return outDir;
    }

    private Bitmap drawDrawable(Drawable drawable, int width, int height, boolean transparent) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(transparent ? Color.TRANSPARENT : Color.WHITE);
        if (drawable != null) {
            Drawable copy = drawable.mutate();
            copy.setBounds(0, 0, width, height);
            copy.draw(canvas);
        }
        return bitmap;
    }

    private Bitmap solidBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);
        return bitmap;
    }

    private Bitmap resizeBitmap(Bitmap source, int width, int height) {
        return Bitmap.createScaledBitmap(source, width, height, true);
    }

    private Bitmap centerOnCanvas(Bitmap source, int width, int height) {
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(Color.TRANSPARENT);
        float x = (width - source.getWidth()) / 2f;
        float y = (height - source.getHeight()) / 2f;
        canvas.drawBitmap(source, x, y, null);
        return out;
    }

    private Bitmap recolorAlpha(Bitmap source, int color) {
        Bitmap out = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        int rgb = color & 0x00ffffff;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = Color.alpha(source.getPixel(x, y));
                out.setPixel(x, y, (alpha << 24) | rgb);
            }
        }
        return out;
    }

    private Bitmap monochromeAlpha(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        List<Integer> lumas = new ArrayList<>();
        int[] sourcePixels = new int[width * height];
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height);
        for (int pixel : sourcePixels) {
            if (Color.alpha(pixel) > 8) {
                lumas.add(luma(pixel));
            }
        }
        int low = percentile(lumas, 0.02);
        int high = percentile(lumas, 0.98);
        boolean hasRange = high - low >= 12;
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] outPixels = new int[sourcePixels.length];
        for (int i = 0; i < sourcePixels.length; i++) {
            int pixel = sourcePixels[i];
            int alpha = Color.alpha(pixel);
            if (alpha <= 0) {
                outPixels[i] = Color.TRANSPARENT;
                continue;
            }
            double maskAlpha;
            if (hasRange) {
                double normalized = (luma(pixel) - low) / (double) (high - low);
                normalized = Math.max(0.0, Math.min(1.0, normalized));
                normalized = Math.pow(normalized, MONO_ALPHA_GAMMA);
                maskAlpha = MONO_ALPHA_MIN + normalized * (MONO_ALPHA_MAX - MONO_ALPHA_MIN);
            } else {
                maskAlpha = MONO_ALPHA_MAX;
            }
            int outAlpha = (int) Math.round((alpha / 255.0) * maskAlpha);
            outPixels[i] = (outAlpha << 24) | 0x00ffffff;
        }
        out.setPixels(outPixels, 0, width, 0, 0, width, height);
        return out;
    }

    private int percentile(List<Integer> values, double ratio) {
        if (values.isEmpty()) {
            return 0;
        }
        values.sort(Integer::compareTo);
        int index = (int) Math.round((values.size() - 1) * ratio);
        index = Math.max(0, Math.min(values.size() - 1, index));
        return values.get(index);
    }

    private int luma(int pixel) {
        return (int) Math.round(Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
    }

    private int sampleColor(Bitmap bitmap) {
        int center = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        if (Color.alpha(center) > 32 && Color.red(center) + Color.green(center) + Color.blue(center) >= 120) {
            return Color.rgb(Color.red(center), Color.green(center), Color.blue(center));
        }
        long r = 0;
        long g = 0;
        long b = 0;
        long count = 0;
        for (int y = 0; y < bitmap.getHeight(); y += 8) {
            for (int x = 0; x < bitmap.getWidth(); x += 8) {
                int pixel = bitmap.getPixel(x, y);
                if (Color.alpha(pixel) >= 128) {
                    r += Color.red(pixel);
                    g += Color.green(pixel);
                    b += Color.blue(pixel);
                    count++;
                }
            }
        }
        if (count == 0) {
            return Color.rgb(216, 224, 253);
        }
        return Color.rgb((int) (r / count), (int) (g / count), (int) (b / count));
    }

    private Bitmap adjustColor(Bitmap source, float saturation, float brightness) {
        Bitmap out = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);
        ColorMatrix brightnessMatrix = new ColorMatrix(new float[]{
                brightness, 0, 0, 0, 0,
                0, brightness, 0, 0, 0,
                0, 0, brightness, 0, 0,
                0, 0, 0, 1, 0
        });
        saturationMatrix.postConcat(brightnessMatrix);
        paint.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        canvas.drawBitmap(source, 0, 0, paint);
        return out;
    }

    private void savePng(Bitmap bitmap, File file) throws Exception {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("无法创建目录: " + parent);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }

    private void ensureCleanDir(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isFile() && child.getName().endsWith(".png")) {
                child.delete();
            }
        }
    }

    private void exportToTree(File packageDir) throws Exception {
        if (outputTreeUri == null) {
            return;
        }
        Uri rootDoc = DocumentsContract.buildDocumentUriUsingTree(
                outputTreeUri,
                DocumentsContract.getTreeDocumentId(outputTreeUri)
        );
        Uri packageDoc = findChild(rootDoc, packageDir.getName());
        if (packageDoc == null) {
            packageDoc = DocumentsContract.createDocument(
                    getContentResolver(),
                    rootDoc,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    packageDir.getName()
            );
        }
        if (packageDoc == null) {
            throw new IllegalStateException("无法创建输出目录");
        }
        File[] files = packageDir.listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            Uri existing = findChild(packageDoc, file.getName());
            if (existing != null) {
                DocumentsContract.deleteDocument(getContentResolver(), existing);
            }
            Uri doc = DocumentsContract.createDocument(getContentResolver(), packageDoc, "image/png", file.getName());
            if (doc == null) {
                throw new IllegalStateException("无法创建文件: " + file.getName());
            }
            try (InputStream in = new FileInputStream(file);
                 OutputStream out = getContentResolver().openOutputStream(doc, "w")) {
                if (out == null) {
                    throw new IllegalStateException("无法写入文件: " + file.getName());
                }
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
    }

    private Uri findChild(Uri parentDoc, String displayName) {
        ContentResolver resolver = getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                outputTreeUri,
                DocumentsContract.getDocumentId(parentDoc)
        );
        try (android.database.Cursor cursor = resolver.query(
                childrenUri,
                new String[]{
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                },
                null,
                null,
                null
        )) {
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToNext()) {
                String childName = cursor.getString(1);
                if (displayName.equals(childName)) {
                    String documentId = cursor.getString(0);
                    return DocumentsContract.buildDocumentUriUsingTree(outputTreeUri, documentId);
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private void installWithRoot(File packageDir, String packageName) throws Exception {
        String target = "/data/oplus/uxicons/" + packageName;
        String source = packageDir.getAbsolutePath();
        String command = "set -e\n"
                + "mkdir -p " + shQuote(target) + "\n"
                + "cp -f " + shQuote(source) + "/*.png " + shQuote(target) + "/\n"
                + "chmod 0644 " + shQuote(target) + "/*.png\n"
                + "restorecon -RF " + shQuote(target) + " 2>/dev/null || true\n";
        Process process = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException("su 退出码: " + code);
        }
    }

    private String shQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private void status(String message) {
        runOnUiThread(() -> statusView.setText(message));
    }

    private static class AppEntry {
        final String label;
        final String packageName;
        final ResolveInfo resolveInfo;

        AppEntry(String label, String packageName, ResolveInfo resolveInfo) {
            this.label = label;
            this.packageName = packageName;
            this.resolveInfo = resolveInfo;
        }
    }
}

@echo off
chcp 65001 >nul
echo ============================================================
echo 拉取APK、提取图标并整理
echo ============================================================
echo.
echo 本批处理将依次执行以下步骤:
echo 1. 拉取缺少PNG图标的应用APK并提取图标
echo 2. 整理并转换提取的图标
echo 3. 清理目录，只保留PNG文件
echo 4. 整理非标准图标，保留分辨率最高的
echo 5. 清理重复图标文件
echo.
pause

echo.
echo ============================================================
echo 步骤 1/5: 拉取APK并提取图标
echo ============================================================
echo.
python cli\extract_missing_apks.py
if errorlevel 1 (
    echo.
    echo 步骤1执行失败，是否继续执行后续步骤？(y/n)
    set /p continue="请输入: "
    if /i not "%continue%"=="y" exit /b 1
)
echo.
pause

echo.
echo ============================================================
echo 步骤 2/5: 整理并转换提取的图标
echo ============================================================
echo.
python cli\convert_and_organize_icons.py
if errorlevel 1 (
    echo.
    echo 步骤2执行失败，是否继续执行后续步骤？(y/n)
    set /p continue="请输入: "
    if /i not "%continue%"=="y" exit /b 1
)
echo.
pause

echo.
echo ============================================================
echo 步骤 3/5: 清理目录只保留PNG文件
echo ============================================================
echo.
python cli\cleanup_and_convert_xml.py
if errorlevel 1 (
    echo.
    echo 步骤3执行失败，是否继续执行后续步骤？(y/n)
    set /p continue="请输入: "
    if /i not "%continue%"=="y" exit /b 1
)
echo.
pause

echo.
echo ============================================================
echo 步骤 4/5: 整理非标准图标保留最高分辨率
echo ============================================================
echo.
python cli\organize_non_standard_icons.py
if errorlevel 1 (
    echo.
    echo 步骤4执行失败，是否继续执行后续步骤？(y/n)
    set /p continue="请输入: "
    if /i not "%continue%"=="y" exit /b 1
)
echo.
pause

echo.
echo ============================================================
echo 步骤 5/5: 清理重复图标文件
echo ============================================================
echo.
python cli\cleanup_duplicate_files.py
echo.
pause

echo.
echo ============================================================
echo 所有步骤执行完成！
echo ============================================================
pause

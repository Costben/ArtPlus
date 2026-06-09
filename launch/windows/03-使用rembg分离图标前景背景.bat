@echo off
chcp 65001 >nul
echo 使用rembg批量分离图标前景和背景
echo ====================================
echo.
echo 规则:
echo - 跳过已有官方ART图标的包
echo - 只处理非官方命名的PNG文件
echo - 使用rembg分离前景和背景
echo.
echo 注意: 首次运行需要下载模型（约170MB）
echo.
echo 依赖要求:
echo - rembg库（已安装）
echo - onnxruntime库（已安装）
echo.
pause
python src\separate_icons_with_rembg.py
echo.
pause

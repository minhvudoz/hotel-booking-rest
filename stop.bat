@echo off
chcp 65001 >nul
cd /d "%~dp0"
docker compose down
echo Đã tắt ứng dụng. Dữ liệu vẫn được giữ cho lần chạy sau.
pause

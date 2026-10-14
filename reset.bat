@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist docker-compose.yml goto notextracted
echo Thao tác này XÓA toàn bộ dữ liệu đã nhập và tạo lại dữ liệu mẫu mới theo ngày hôm nay.
echo Nhấn phím bất kỳ để tiếp tục, hoặc đóng cửa sổ này để hủy.
pause >nul
docker compose down -v
call start.bat
exit /b 0

:notextracted
echo Hãy giải nén toàn bộ file ZIP ra một thư mục, rồi bấm đúp reset.bat trong thư mục đó.
pause
exit /b 1

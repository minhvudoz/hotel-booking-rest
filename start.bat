@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Quan ly Khach san

where docker >nul 2>nul
if errorlevel 1 goto nodocker
docker info >nul 2>nul
if errorlevel 1 goto notrunning

echo Đang khởi động ứng dụng, lần đầu có thể mất 3-5 phút...
docker compose up -d --build
if errorlevel 1 goto failed

set tries=0
:wait
curl -fs http://localhost:8080/api/health >nul 2>nul
if not errorlevel 1 goto ready
set /a tries+=1
if %tries% geq 90 goto timeout
timeout /t 2 /nobreak >nul
goto wait

:ready
echo.
echo Ứng dụng đã sẵn sàng tại http://localhost:8080
echo Tài khoản: admin / admin123 hoặc letan / letan123
start "" http://localhost:8080
timeout /t 15 >nul
exit /b 0

:nodocker
echo Máy chưa cài Docker Desktop.
echo Trình duyệt sẽ mở trang tải. Cài xong, mở Docker Desktop rồi bấm đúp lại file start.bat
start "" https://www.docker.com/products/docker-desktop/
pause
exit /b 1

:notrunning
echo Docker Desktop chưa chạy. Hãy mở Docker Desktop, đợi khoảng 1 phút rồi bấm đúp lại file start.bat
pause
exit /b 1

:failed
echo Khởi động thất bại. Có thể cổng 8080 đang bị chương trình khác dùng.
echo Hãy chụp màn hình cửa sổ này gửi cho người hỗ trợ.
pause
exit /b 1

:timeout
echo Ứng dụng chưa phản hồi sau 3 phút. Hãy chụp màn hình cửa sổ này gửi cho người hỗ trợ.
docker compose logs --tail 40 app
pause
exit /b 1

#!/usr/bin/env bash
cd "$(dirname "$0")" || exit 1
URL=http://localhost:8080
pause() { read -r -p "Nhấn Enter để đóng..." _; }

if [ ! -f docker-compose.yml ]; then
  echo "Không tìm thấy file docker-compose.yml. Hãy giải nén toàn bộ file ZIP ra một thư mục rồi chạy lại từ thư mục đó."
  pause; exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Máy chưa cài Docker Desktop. Tải tại: https://www.docker.com/products/docker-desktop/"
  pause; exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop chưa chạy. Hãy mở Docker Desktop, đợi khoảng 1 phút rồi chạy lại."
  pause; exit 1
fi

echo "Đang khởi động ứng dụng, lần đầu có thể mất 3-5 phút..."
if ! docker compose up -d --build; then
  echo "Khởi động thất bại, xem thông báo lỗi phía trên (thường gặp nhất: cổng 8080 đang bị chương trình khác dùng)."
  echo "Hãy chụp màn hình cửa sổ này gửi cho người hỗ trợ."
  pause; exit 1
fi

for _ in $(seq 1 90); do
  if curl -fs "$URL/api/health" >/dev/null 2>&1; then
    echo "Ứng dụng đã sẵn sàng tại $URL"
    echo "Tài khoản: admin / admin123 hoặc letan / letan123"
    if command -v open >/dev/null 2>&1; then open "$URL"; else xdg-open "$URL" >/dev/null 2>&1; fi
    exit 0
  fi
  sleep 2
done
echo "Ứng dụng chưa phản hồi sau 3 phút."
docker compose logs --tail 40 app
pause; exit 1

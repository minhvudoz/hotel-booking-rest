#!/bin/bash
cd "$(dirname "$0")" || exit 1
echo "Thao tác này XÓA toàn bộ dữ liệu đã nhập và tạo lại dữ liệu mẫu mới theo ngày hôm nay."
read -r -p "Nhấn Enter để tiếp tục (đóng cửa sổ này để hủy)..." _
docker compose down -v && exec bash ./start.sh

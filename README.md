# 🏨 Hotel Booking REST API

![API tests](https://github.com/minhvudoz/hotel-booking-rest/actions/workflows/ci.yml/badge.svg)

Dịch vụ web RESTful quản lý đặt phòng khách sạn, xây dựng bằng **JAX-RS (Jersey 2)** trên **Tomcat 9**, CSDL **PostgreSQL**, có giao diện web tiếng Việt, xác thực **JWT**, tài liệu **Swagger** và bộ kiểm thử **Postman**.

## Chạy thử nhanh nhất

| Cách | Cần gì | Làm thế nào |
|---|---|---|
| ☁️ GitHub Codespaces | Tài khoản GitHub | [![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/minhvudoz/hotel-booking-rest), đợi 3–5 phút, tab ứng dụng tự mở (nếu trang báo lỗi thì đợi thêm 1 phút rồi tải lại) |
| 💻 Máy Windows | Docker Desktop | **Giải nén** file ZIP, bấm đúp `start.bat`, trình duyệt tự mở `http://localhost:8080`. Tắt bằng `stop.bat` |
| 🍎 Máy Mac / Linux | Docker Desktop | **Giải nén** file ZIP, bấm đúp `start.command` (Mac) hoặc chạy `./start.sh`. Tắt bằng `stop.command`. Lần đầu trên Mac nếu bị chặn: chuột phải vào file → **Open**, hoặc vào System Settings → Privacy & Security → **Open Anyway** |
| 🌐 Link online | Không cần gì | **https://hotel-booking-rest.onrender.com** (tài liệu API: [/docs](https://hotel-booking-rest.onrender.com/docs/)). Nếu lâu không ai mở, lần đầu mất khoảng 30–60 giây để "đánh thức" |

**Làm mới dữ liệu mẫu** (nên làm trước khi demo nếu đã chạy từ nhiều ngày trước): bấm đúp `reset.bat` (Windows) hoặc `reset.command` (Mac). Thao tác này xóa dữ liệu đã nhập và tạo lại dữ liệu mẫu theo ngày hôm nay.

**Tài khoản dùng thử:** `admin / admin123` (quản trị viên) · `letan / letan123` (lễ tân)

## Đưa lên mạng miễn phí (Render + Neon)

Tạo một link công khai để ai cũng mở được, không tốn tiền, không cần thẻ:

1. **Neon** (CSDL): đăng nhập [neon.tech](https://neon.tech) bằng GitHub → **Create project** (region **AWS Asia Pacific (Singapore)**) → bấm **Connect** → copy chuỗi kết nối dạng `postgresql://...neon.tech/neondb?sslmode=require...`
2. **Render** (app): đăng nhập [render.com](https://render.com) bằng GitHub → **New → Blueprint** → chọn repo này → ô `DATABASE_URL` dán chuỗi vừa copy → **Apply**
3. Đợi khoảng 5–10 phút để build xong, rồi mở link `https://hotel-booking-rest-xxxx.onrender.com`. Lần đầu ứng dụng tự tạo bảng và dữ liệu mẫu.

Gói free của Render cho app "ngủ" sau 15 phút không ai truy cập, lần mở sau mất khoảng 30–60 giây để thức dậy.

## Chức năng

- Sơ đồ phòng theo ngày, tìm phòng trống, đặt phòng (chặn trùng lịch, kể cả khi hai người đặt cùng lúc)
- Nhận phòng, ghi dịch vụ phát sinh, trả phòng kèm hóa đơn và thanh toán, in hóa đơn
- Quản lý khách hàng và lịch sử lưu trú
- Quản trị phòng và danh mục dịch vụ (chỉ admin)
- Dashboard: doanh thu 12 tháng, công suất phòng, số liệu trong ngày (chỉ admin)
- Tài liệu API tương tác tại `/docs`

## API

Tiền tố `/api`, xác thực bằng header `Authorization: Bearer <token>` (lấy từ `POST /api/auth/login`). Xem đầy đủ tại `/docs`.

| Nhóm | Endpoint chính |
|---|---|
| Xác thực | `POST /auth/login`, `GET /auth/me` |
| Phòng | `GET/POST /rooms`, `GET/PUT/DELETE /rooms/{id}`, `GET /rooms/available`, `GET /rooms/status` |
| Khách hàng | `GET/POST /customers`, `GET/PUT /customers/{id}`, `GET /customers/{id}/bookings` |
| Dịch vụ | `GET/POST /amenities`, `PUT/DELETE /amenities/{id}` |
| Đặt phòng | `GET/POST /bookings`, `GET /bookings/{id}`, `PUT /bookings/{id}/cancel`, `/check-in`, `/check-out`, `GET/POST /bookings/{id}/services`, `DELETE /bookings/{id}/services/{sid}`, `GET /bookings/{id}/invoice` |
| Thống kê | `GET /stats/overview`, `/stats/revenue`, `/stats/occupancy` |

## Kiểm thử bằng Postman

1. `node postman/build.mjs` để sinh `postman/HotelBooking.postman_collection.json`
2. Postman → Import → chọn file trên → chạy cả collection (Run collection), biến `baseUrl` mặc định là `http://localhost:8080`
3. Hoặc chạy tự động toàn bộ trên một stack tạm: `./scripts/dev-test.sh`

## Mở bằng NetBeans

File → Open Project → chọn thư mục này (NetBeans nhận dạng dự án Maven). Bật CSDL bằng `docker compose up -d db` (Postgres tại `jdbc:postgresql://localhost:55432/hotel`, user `hotel`, mật khẩu xem trong `docker-compose.yml`). Thêm server Apache Tomcat 9, khai báo biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` cho Tomcat, rồi bấm Run.

## Công nghệ

Java 17 · Maven · Tomcat 9 · Jersey 2.48 (JAX-RS) · Jackson · PostgreSQL 16 · HikariCP · java-jwt · jBCrypt · swagger-jaxrs2 + Swagger UI · Chart.js · Docker Compose · Postman/Newman · GitHub Actions

import { folder, req, status, check, save, error, v } from './lib.mjs';

const newRoom = ["pm.collectionVariables.set('roomNumber', 'T' + Date.now().toString().slice(-6));"];
const tempRoom = ["pm.collectionVariables.set('tempRoomNumber', 'X' + Date.now().toString().slice(-6));"];

export default folder('2. Phòng', [
  req('Thêm phòng (admin)', 'POST', '/rooms', {
    pre: newRoom,
    body: '{"roomNumber": "{{roomNumber}}", "type": "DELUXE", "pricePerNight": 800000, "capacity": 2, "description": "Phòng tạo bởi Postman"}',
    tests: [status(201), check('Trả về phòng vừa tạo', `body.id > 0 && body.roomNumber === ${v('roomNumber')}`), save('roomId', 'body.id')],
  }),
  req('Thêm phòng trùng số → 409', 'POST', '/rooms', {
    body: '{"roomNumber": "{{roomNumber}}", "type": "DELUXE", "pricePerNight": 800000, "capacity": 2}',
    tests: [error(409)],
  }),
  req('Lễ tân thêm phòng → 403', 'POST', '/rooms', {
    auth: 'letan', body: { roomNumber: 'Z999', type: 'SUITE', pricePerNight: 1000000, capacity: 2 }, tests: [error(403)],
  }),
  req('Loại phòng và giá không hợp lệ → 400', 'POST', '/rooms', {
    body: { roomNumber: 'Z998', type: 'VIP', pricePerNight: 0, capacity: 2 }, tests: [error(400)],
  }),
  req('JSON sai cú pháp → 400', 'POST', '/rooms', { body: '{"roomNumber": ', tests: [error(400)] }),
  req('Số phòng quá dài (> 10 ký tự) → 400', 'POST', '/rooms', { body: { roomNumber: 'Phòng VIP 01', type: 'SUITE', pricePerNight: 2000000, capacity: 2 }, tests: [error(400)] }),
  req('Danh sách phòng trang 1', 'GET', '/rooms?page=1&size=5', {
    auth: 'letan',
    tests: [status(200), check('Đúng cấu trúc trang', 'Array.isArray(body.items) && body.items.length === 5 && body.page === 1 && body.size === 5 && body.total >= 13')],
  }),
  req('Trang vượt quá trang cuối trả danh sách rỗng', 'GET', '/rooms?page=999&size=5', {
    tests: [status(200), check('items rỗng', 'body.items.length === 0 && body.page === 999')],
  }),
  req('size quá lớn bị giới hạn 100, page=0 thành 1', 'GET', '/rooms?page=0&size=500', {
    tests: [status(200), check('size = 100, page = 1', 'body.size === 100 && body.page === 1')],
  }),
  req('Lọc theo loại (không phân biệt hoa thường)', 'GET', '/rooms?type=suite&size=100', {
    tests: [status(200), check('Chỉ có SUITE', "body.items.length >= 3 && body.items.every(r => r.type === 'SUITE')")],
  }),
  req('Loại phòng không tồn tại → 400', 'GET', '/rooms?type=VIP', { tests: [error(400)] }),
  req('Chi tiết phòng', 'GET', '/rooms/{{roomId}}', { tests: [status(200), check('Đúng phòng', `body.id == ${v('roomId')}`)] }),
  req('Phòng không tồn tại → 404', 'GET', '/rooms/999999', { tests: [error(404)] }),
  req('Sửa phòng', 'PUT', '/rooms/{{roomId}}', {
    body: '{"roomNumber": "{{roomNumber}}", "type": "SUITE", "pricePerNight": 1000000, "capacity": 3, "description": "Đã nâng cấp"}',
    tests: [status(200), check('Đã cập nhật', "body.type === 'SUITE' && body.pricePerNight === 1000000 && body.description === 'Đã nâng cấp'")],
  }),
  req('Phòng trống theo khoảng ngày', 'GET', '/rooms/available?checkIn={{future1}}&checkOut={{future2}}', {
    auth: 'letan', tests: [status(200), check('Có phòng test vừa tạo', `body.some(r => r.id == ${v('roomId')})`)],
  }),
  req('Ngày trả trước ngày nhận → 400', 'GET', '/rooms/available?checkIn={{tomorrow}}&checkOut={{today}}', { tests: [error(400)] }),
  req('Ngày sai định dạng → 400', 'GET', '/rooms/available?checkIn=01-11-2026&checkOut={{today}}', { tests: [error(400)] }),
  req('Trạng thái phòng hôm nay', 'GET', '/rooms/status?date={{today}}', {
    auth: 'letan',
    tests: [status(200), check('Mỗi phòng có trạng thái hợp lệ', "body.length >= 13 && body.every(s => ['AVAILABLE', 'BOOKED', 'OCCUPIED'].includes(s.status))")],
  }),
  req('Thêm phòng tạm để xóa', 'POST', '/rooms', {
    pre: tempRoom,
    body: '{"roomNumber": "{{tempRoomNumber}}", "type": "STANDARD", "pricePerNight": 400000, "capacity": 2}',
    tests: [status(201), save('tempRoomId', 'body.id')],
  }),
  req('Xóa phòng chưa có đơn', 'DELETE', '/rooms/{{tempRoomId}}', { tests: [status(204)] }),
  req('Phòng đã xóa → 404', 'GET', '/rooms/{{tempRoomId}}', { tests: [error(404)] }),
]);

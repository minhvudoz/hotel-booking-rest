import { folder, req, status, check, save, error, v } from './lib.mjs';

const body = (ci, co) => `{"roomId": {{roomId}}, "customerId": {{customerId}}, "checkIn": "{{${ci}}}", "checkOut": "{{${co}}}"}`;

export default folder('5. Đặt phòng', [
  req('Ngày trả trước ngày nhận → 400', 'POST', '/bookings', { auth: 'letan', body: body('tomorrow', 'today'), tests: [error(400)] }),
  req('Ngày nhận ở quá khứ → 400', 'POST', '/bookings', { auth: 'letan', body: body('yesterday', 'tomorrow'), tests: [error(400)] }),
  req('Ngày sai định dạng trong body → 400', 'POST', '/bookings', {
    auth: 'letan', body: '{"roomId": {{roomId}}, "customerId": {{customerId}}, "checkIn": "2026-13-45", "checkOut": "{{tomorrow}}"}', tests: [error(400)],
  }),
  req('Thiếu thông tin → 400', 'POST', '/bookings', { auth: 'letan', body: '{"roomId": {{roomId}}}', tests: [error(400)] }),
  req('Phòng không tồn tại → 404', 'POST', '/bookings', {
    auth: 'letan', body: '{"roomId": 999999, "customerId": {{customerId}}, "checkIn": "{{today}}", "checkOut": "{{tomorrow}}"}', tests: [error(404)],
  }),
  req('Đặt phòng hôm nay → ngày mai', 'POST', '/bookings', {
    auth: 'letan', body: body('today', 'tomorrow'),
    tests: [status(201),
      check('Đơn mới: BOOKED, 1 đêm, 1.000.000đ, do lễ tân tạo', "body.status === 'BOOKED' && body.nights === 1 && body.roomTotal === 1000000 && body.createdBy === 'letan'"),
      save('bookingId', 'body.id')],
  }),
  req('Đặt trùng lịch → 409', 'POST', '/bookings', {
    auth: 'letan', body: body('today', 'tomorrow'),
    tests: [error(409), check('Thông điệp nêu rõ trùng lịch', "body.error.includes('đã có khách đặt')")],
  }),
  req('Thêm dịch vụ khi chưa nhận phòng → 409', 'POST', '/bookings/{{bookingId}}/services', {
    auth: 'letan', body: '{"amenityId": {{amenityId}}, "quantity": 1}', tests: [error(409)],
  }),
  req('Trả phòng khi chưa nhận phòng → 409', 'PUT', '/bookings/{{bookingId}}/check-out', { auth: 'letan', body: { paymentMethod: 'CASH' }, tests: [error(409)] }),
  req('Nhận phòng', 'PUT', '/bookings/{{bookingId}}/check-in', { auth: 'letan', tests: [status(200), check('Đang ở', "body.status === 'CHECKED_IN'")] }),
  req('Nhận phòng lần 2 → 409', 'PUT', '/bookings/{{bookingId}}/check-in', { auth: 'letan', tests: [error(409)] }),
  req('Ghi dịch vụ phát sinh', 'POST', '/bookings/{{bookingId}}/services', {
    auth: 'letan', body: '{"amenityId": {{amenityId}}, "quantity": 2}',
    tests: [status(201), check('Thành tiền = 2 × 100.000', 'body.amount === 200000 && body.unitPrice === 100000')],
  }),
  req('Số lượng 0 → 400', 'POST', '/bookings/{{bookingId}}/services', { auth: 'letan', body: '{"amenityId": {{amenityId}}, "quantity": 0}', tests: [error(400)] }),
  req('Ghi nhầm thêm 1 dịch vụ', 'POST', '/bookings/{{bookingId}}/services', {
    auth: 'letan', body: '{"amenityId": 1, "quantity": 1}', tests: [status(201), save('wrongUsageId', 'body.id')],
  }),
  req('Xóa dịch vụ ghi nhầm', 'DELETE', '/bookings/{{bookingId}}/services/{{wrongUsageId}}', { auth: 'letan', tests: [status(204)] }),
  req('Dịch vụ đã dùng của đơn', 'GET', '/bookings/{{bookingId}}/services', {
    auth: 'letan', tests: [status(200), check('Còn đúng 1 dịch vụ', `body.length === 1 && body[0].amenityId == ${v('amenityId')}`)],
  }),
  req('Xem trước hóa đơn', 'GET', '/bookings/{{bookingId}}/invoice', {
    auth: 'letan', tests: [status(200), check('Tổng 1.200.000, chưa thanh toán', 'body.total === 1200000 && body.servicesTotal === 200000 && body.payment === null')],
  }),
  req('Hình thức thanh toán sai → 400', 'PUT', '/bookings/{{bookingId}}/check-out', { auth: 'letan', body: { paymentMethod: 'BITCOIN' }, tests: [error(400)] }),
  req('Trả phòng và thanh toán chuyển khoản', 'PUT', '/bookings/{{bookingId}}/check-out', {
    auth: 'letan', body: { paymentMethod: 'TRANSFER' },
    tests: [status(200), check('Đã thanh toán đủ, trạng thái CHECKED_OUT',
      "body.total === 1200000 && body.payment.amount === 1200000 && body.payment.method === 'TRANSFER' && body.booking.status === 'CHECKED_OUT'")],
  }),
  req('Hủy đơn đã trả phòng → 409', 'PUT', '/bookings/{{bookingId}}/cancel', { auth: 'letan', tests: [error(409)] }),
  req('Thêm dịch vụ sau khi trả phòng → 409', 'POST', '/bookings/{{bookingId}}/services', {
    auth: 'letan', body: '{"amenityId": {{amenityId}}, "quantity": 1}', tests: [error(409)],
  }),
  req('Đặt phòng tương lai', 'POST', '/bookings', { auth: 'letan', body: body('future1', 'future2'), tests: [status(201), save('futureBookingId', 'body.id')] }),
  req('Nhận phòng trước ngày → 409', 'PUT', '/bookings/{{futureBookingId}}/check-in', { auth: 'letan', tests: [error(409)] }),
  req('Hủy đơn tương lai', 'PUT', '/bookings/{{futureBookingId}}/cancel', { auth: 'letan', tests: [status(200), check('Đã hủy', "body.status === 'CANCELLED'")] }),
  req('Đặt lại khoảng ngày vừa hủy', 'POST', '/bookings', { auth: 'letan', body: body('future1', 'future2'), tests: [status(201)] }),
  req('Lọc đơn đã hủy', 'GET', '/bookings?status=CANCELLED&size=100', {
    auth: 'letan', tests: [status(200), check('Toàn bộ là CANCELLED', "body.items.length > 0 && body.items.every(b => b.status === 'CANCELLED')")],
  }),
  req('Tìm đơn theo SĐT khách', 'GET', '/bookings?q={{customerPhone}}', { auth: 'letan', tests: [status(200), check('Có 3 đơn của khách test', 'body.total === 3')] }),
  req('Trạng thái đơn không hợp lệ → 400', 'GET', '/bookings?status=DONE', { auth: 'letan', tests: [error(400)] }),
  req('Lịch sử lưu trú của khách', 'GET', '/customers/{{customerId}}/bookings', { auth: 'letan', tests: [status(200), check('3 lượt', 'body.length === 3')] }),
  req('Xóa phòng đã có đơn → 409', 'DELETE', '/rooms/{{roomId}}', { tests: [error(409)] }),
  req('Xóa dịch vụ đã được dùng → 409', 'DELETE', '/amenities/{{amenityId}}', { tests: [error(409)] }),
  req('Đơn không tồn tại → 404', 'GET', '/bookings/999999', { auth: 'letan', tests: [error(404)] }),
]);

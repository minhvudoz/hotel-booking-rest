import { folder, req, status, check, save, error, v } from './lib.mjs';

const newPhone = ["pm.collectionVariables.set('customerPhone', '09' + Date.now().toString().slice(-8));"];

export default folder('3. Khách hàng', [
  req('Thêm khách (lễ tân)', 'POST', '/customers', {
    auth: 'letan', pre: newPhone,
    body: '{"fullName": "Nguyễn Thị Tèo", "phone": "{{customerPhone}}", "idCard": "001099000999"}',
    tests: [status(201), check('Giữ nguyên tiếng Việt có dấu', "body.fullName === 'Nguyễn Thị Tèo' && body.id > 0"), save('customerId', 'body.id')],
  }),
  req('Trùng số điện thoại → 409', 'POST', '/customers', {
    auth: 'letan', body: '{"fullName": "Người khác", "phone": "{{customerPhone}}"}', tests: [error(409)],
  }),
  req('Số điện thoại sai → 400', 'POST', '/customers', { auth: 'letan', body: { fullName: 'Test', phone: '12345' }, tests: [error(400)] }),
  req('Thiếu họ tên → 400', 'POST', '/customers', { auth: 'letan', body: { phone: '0999999999' }, tests: [error(400)] }),
  req('CCCD quá dài (> 20 ký tự) → 400', 'POST', '/customers', { auth: 'letan', body: { fullName: 'Test dài', phone: '0988888888', idCard: '0123456789012345678901' }, tests: [error(400)] }),
  req('Tìm theo số điện thoại', 'GET', '/customers?q={{customerPhone}}', {
    auth: 'letan', tests: [status(200), check('Tìm đúng 1 khách', `body.total === 1 && body.items[0].id == ${v('customerId')}`)],
  }),
  req('Tìm theo tên có dấu', 'GET', '/customers?q=th%E1%BB%8B%20t%C3%A8o', {
    tests: [status(200), check('Không phân biệt hoa thường', `body.items.some(c => c.id == ${v('customerId')})`)],
  }),
  req('Chi tiết khách', 'GET', '/customers/{{customerId}}', { auth: 'letan', tests: [status(200), check('Đúng SĐT', `body.phone === ${v('customerPhone')}`)] }),
  req('Khách không tồn tại → 404', 'GET', '/customers/999999', { tests: [error(404)] }),
  req('Sửa khách', 'PUT', '/customers/{{customerId}}', {
    auth: 'letan', body: '{"fullName": "Nguyễn Thị Tèo", "phone": "{{customerPhone}}", "idCard": "001099000999", "email": "teo@example.com"}',
    tests: [status(200), check('Đã có email', "body.email === 'teo@example.com'")],
  }),
  req('Email sai → 400', 'PUT', '/customers/{{customerId}}', {
    auth: 'letan', body: '{"fullName": "Nguyễn Thị Tèo", "phone": "{{customerPhone}}", "email": "khong-phai-email"}', tests: [error(400)],
  }),
]);

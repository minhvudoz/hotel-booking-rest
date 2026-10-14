import { folder, req, status, check, error } from './lib.mjs';

export default folder('0. Hệ thống', [
  req('Health check', 'GET', '/health', {
    auth: null,
    tests: [status(200), check('App và CSDL đang chạy', "body.status === 'UP' && body.database === 'UP'")],
  }),
  req('Đường dẫn không tồn tại trả lỗi JSON', 'GET', '/khong-ton-tai', { auth: null, tests: [error(404)] }),
]);

import { folder, req, status, check, save, error } from './lib.mjs';

export default folder('1. Xác thực', [
  req('Đăng nhập admin', 'POST', '/auth/login', {
    auth: null,
    body: { username: 'admin', password: 'admin123' },
    tests: [status(200), check('Có token, vai trò ADMIN', "typeof body.token === 'string' && body.user.role === 'ADMIN'"), save('adminToken', 'body.token')],
  }),
  req('Đăng nhập lễ tân', 'POST', '/auth/login', {
    auth: null,
    body: { username: 'letan', password: 'letan123' },
    tests: [status(200), check('Vai trò RECEPTIONIST', "body.user.role === 'RECEPTIONIST'"), save('letanToken', 'body.token')],
  }),
  req('Sai mật khẩu → 401', 'POST', '/auth/login', { auth: null, body: { username: 'admin', password: 'sai-mat-khau' }, tests: [error(401)] }),
  req('Thiếu thông tin đăng nhập → 400', 'POST', '/auth/login', { auth: null, body: { username: 'admin' }, tests: [error(400)] }),
  req('Không gửi token → 401', 'GET', '/auth/me', { auth: null, tests: [error(401)] }),
  req('Token giả → 401', 'GET', '/auth/me', { auth: 'invalid', tests: [error(401)] }),
  req('Thông tin người đăng nhập', 'GET', '/auth/me', {
    auth: 'letan',
    tests: [status(200), check('Đúng lễ tân, không lộ mật khẩu', "body.username === 'letan' && body.passwordHash === undefined")],
  }),
]);

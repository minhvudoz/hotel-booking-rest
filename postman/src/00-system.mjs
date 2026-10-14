import { folder, req, status, check, error } from './lib.mjs';

export default folder('0. Hệ thống', [
  {
    name: 'Trang Swagger UI',
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: [status(200), "pm.test('Là trang HTML Swagger', () => pm.expect(pm.response.text()).to.include('swagger-ui'));"] } }],
    request: { method: 'GET', header: [], url: '{{baseUrl}}/docs/' },
  },
  {
    name: 'Thư viện Swagger UI được đóng gói sẵn',
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: [status(200)] } }],
    request: { method: 'GET', header: [], url: '{{baseUrl}}/webjars/swagger-ui/5.32.15/swagger-ui-bundle.js' },
  },
  {
    name: 'Trang giao diện web',
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: [status(200), "pm.test('Nạp ứng dụng JS', () => pm.expect(pm.response.text()).to.include('js/app.js'));"] } }],
    request: { method: 'GET', header: [], url: '{{baseUrl}}/' },
  },
  {
    name: 'Thư viện Chart.js được đóng gói sẵn',
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: [status(200)] } }],
    request: { method: 'GET', header: [], url: '{{baseUrl}}/webjars/chart.js/4.5.1/dist/chart.umd.min.js' },
  },
  req('Health check', 'GET', '/health', {
    auth: null,
    tests: [status(200), check('App và CSDL đang chạy', "body.status === 'UP' && body.database === 'UP'")],
  }),
  req('Đường dẫn không tồn tại trả lỗi JSON', 'GET', '/khong-ton-tai', { auth: null, tests: [error(404)] }),
  req('Đặc tả OpenAPI', 'GET', '/openapi.json', {
    auth: null,
    tests: [status(200), check('Có đủ các nhóm API chính', "['/api/rooms', '/api/customers', '/api/amenities', '/api/bookings', '/api/stats/overview', '/api/auth/login'].every(p => Object.keys(body.paths).includes(p))")],
  }),
]);

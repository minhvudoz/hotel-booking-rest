import { folder, req, status, check, save, error } from './lib.mjs';

const newName = ["pm.collectionVariables.set('amenityName', 'Dịch vụ test ' + Date.now());"];
const tempName = ["pm.collectionVariables.set('tempAmenityName', 'Dịch vụ tạm ' + Date.now());"];

export default folder('4. Dịch vụ', [
  req('Danh mục dịch vụ (lễ tân)', 'GET', '/amenities', { auth: 'letan', tests: [status(200), check('Có ít nhất 6 dịch vụ mẫu', 'body.length >= 6')] }),
  req('Thêm dịch vụ (admin)', 'POST', '/amenities', {
    pre: newName, body: '{"name": "{{amenityName}}", "unitPrice": 120000, "unit": "lượt"}',
    tests: [status(201), check('Đúng đơn giá', 'body.unitPrice === 120000'), save('amenityId', 'body.id')],
  }),
  req('Lễ tân thêm dịch vụ → 403', 'POST', '/amenities', { auth: 'letan', body: { name: 'Không được', unitPrice: 1, unit: 'lượt' }, tests: [error(403)] }),
  req('Trùng tên dịch vụ → 409', 'POST', '/amenities', { body: '{"name": "{{amenityName}}", "unitPrice": 1000, "unit": "lượt"}', tests: [error(409)] }),
  req('Đơn giá không hợp lệ → 400', 'POST', '/amenities', { body: { name: 'Sai giá', unitPrice: -5, unit: 'lượt' }, tests: [error(400)] }),
  req('Sửa dịch vụ', 'PUT', '/amenities/{{amenityId}}', {
    body: '{"name": "{{amenityName}}", "unitPrice": 100000, "unit": "lượt"}', tests: [status(200), check('Đã đổi giá', 'body.unitPrice === 100000')],
  }),
  req('Thêm dịch vụ tạm để xóa', 'POST', '/amenities', {
    pre: tempName, body: '{"name": "{{tempAmenityName}}", "unitPrice": 5000, "unit": "món"}', tests: [status(201), save('tempAmenityId', 'body.id')],
  }),
  req('Xóa dịch vụ chưa dùng', 'DELETE', '/amenities/{{tempAmenityId}}', { tests: [status(204)] }),
  req('Xóa dịch vụ không tồn tại → 404', 'DELETE', '/amenities/{{tempAmenityId}}', { tests: [error(404)] }),
]);

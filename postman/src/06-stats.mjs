import { folder, req, status, check, error } from './lib.mjs';

const thisMonth = 'new Date(Date.now() + 7 * 3600e3).getUTCMonth() + 1';

export default folder('6. Thống kê', [
  req('Tổng quan hôm nay (admin)', 'GET', '/stats/overview', {
    tests: [status(200), check('Số liệu hợp lệ', 'body.totalRooms >= 13 && body.available >= 0 && body.available <= body.totalRooms && body.occupied >= 0')],
  }),
  req('Lễ tân xem thống kê → 403', 'GET', '/stats/overview', { auth: 'letan', tests: [error(403)] }),
  req('Doanh thu 12 tháng năm nay', 'GET', '/stats/revenue', {
    tests: [status(200), check('Đủ 12 tháng, tháng này có khoản 1.200.000 vừa thu',
      `body.length === 12 && body.every((m, i) => m.month === i + 1) && body.find(m => m.month === ${thisMonth}).revenue >= 1200000`)],
  }),
  req('Năm không hợp lệ → 400', 'GET', '/stats/revenue?year=1800', { tests: [error(400)] }),
  req('Công suất phòng 30 ngày gần nhất', 'GET', '/stats/occupancy', {
    tests: [status(200), check('30 ngày, tỷ lệ 0–100%', 'body.length === 30 && body.every(d => d.rate >= 0 && d.rate <= 100)')],
  }),
  req('Khoảng ngày ngược → 400', 'GET', '/stats/occupancy?from={{tomorrow}}&to={{today}}', { tests: [error(400)] }),
]);

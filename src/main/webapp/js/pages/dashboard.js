import { h, money } from '../ui.js';
import { api, guard } from '../api.js';

const stat = (label, value, kind) => h('div', { class: `card stat ${kind}` }, h('div', { class: 'num' }, value), h('div', {}, label));

export function dashboardPage(root) {
  const cards = h('div', { class: 'cards' });
  const revenueCanvas = h('canvas');
  const occupancyCanvas = h('canvas');
  root.append(
    h('h2', {}, 'Tổng quan hôm nay'),
    cards,
    h('div', { class: 'grid2' },
      h('div', { class: 'card' }, h('h3', {}, `Doanh thu năm ${new Date().getFullYear()}`), revenueCanvas),
      h('div', { class: 'card' }, h('h3', {}, 'Công suất phòng 30 ngày gần nhất'), occupancyCanvas)));

  guard(async () => {
    const [overview, revenue, occupancy] = await Promise.all([
      api('GET', '/stats/overview'), api('GET', '/stats/revenue'), api('GET', '/stats/occupancy')]);
    cards.append(
      stat('Phòng trống', overview.available, 'ok'),
      stat('Phòng đang có khách', overview.occupied, 'danger'),
      stat('Khách sắp nhận phòng', overview.arrivals, 'warn'),
      stat('Khách sắp trả phòng', overview.departures, ''));
    new Chart(revenueCanvas, {
      type: 'bar',
      data: { labels: revenue.map(m => 'T' + m.month), datasets: [{ data: revenue.map(m => m.revenue), backgroundColor: '#2f6fd6' }] },
      options: {
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => money(c.raw) } } },
        scales: { y: { ticks: { callback: v => v / 1e6 + ' tr' } } },
      },
    });
    new Chart(occupancyCanvas, {
      type: 'line',
      data: {
        labels: occupancy.map(d => d.date.slice(8) + '/' + d.date.slice(5, 7)),
        datasets: [{ data: occupancy.map(d => d.rate), borderColor: '#c53030', tension: 0.3 }],
      },
      options: { plugins: { legend: { display: false } }, scales: { y: { min: 0, max: 100, ticks: { callback: v => v + '%' } } } },
    });
  })();
}

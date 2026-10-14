import { h, money, date, badge, STATUS, pager, table } from '../ui.js';
import { api, guard, qs } from '../api.js';
import { openServices, openCheckout, openInvoice } from './booking-actions.js';

export function bookingsPage(root) {
  const state = { status: '', q: '', page: 1 };
  const rows = h('tbody');
  const footer = h('div');
  const status = h('select', {}, h('option', { value: '' }, 'Tất cả trạng thái'),
    ['BOOKED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'].map(s => h('option', { value: s }, STATUS[s][0])));
  const search = h('input', { placeholder: 'Tên, SĐT khách hoặc số phòng' });
  const go = () => { state.status = status.value; state.q = search.value.trim(); state.page = 1; load(); };
  status.addEventListener('change', go);
  search.addEventListener('keydown', e => { if (e.key === 'Enter') go(); });

  const load = guard(async () => {
    const page = await api('GET', '/bookings' + qs(state));
    rows.replaceChildren(...(page.items.length ? page.items.map(b => h('tr', {},
      h('td', {}, '#' + b.id),
      h('td', {}, b.roomNumber),
      h('td', {}, b.customerName, h('br'), h('small', {}, b.customerPhone)),
      h('td', {}, `${date(b.checkIn)} – ${date(b.checkOut)}`, h('br'), h('small', {}, `${b.nights} đêm`)),
      h('td', { class: 'num' }, money(b.roomTotal)),
      h('td', {}, badge(b.status)),
      h('td', { class: 'row-actions' }, actions(b, load)))) : [h('tr', {}, h('td', { colspan: 7 }, 'Không có đơn nào.'))]));
    footer.replaceChildren(pager(page, p => { state.page = p; load(); }));
  });

  root.append(h('h2', {}, 'Đơn đặt phòng'), h('div', { class: 'card' },
    h('div', { class: 'toolbar' }, status, search, h('button', { class: 'btn', onclick: go }, 'Tìm')),
    table(['Mã', 'Phòng', 'Khách', 'Nhận – Trả', 'Tiền phòng', 'Trạng thái', ''], rows),
    footer));
  load();
}

function actions(b, reload) {
  const button = (label, kind, fn) => h('button', { class: `btn ${kind}`, onclick: guard(fn) }, label);
  switch (b.status) {
    case 'BOOKED':
      return [
        button('Nhận phòng', 'primary', async () => { await api('PUT', `/bookings/${b.id}/check-in`); reload(); }),
        button('Hủy', 'ghost', async () => {
          if (confirm(`Hủy đơn #${b.id} của ${b.customerName}?`)) { await api('PUT', `/bookings/${b.id}/cancel`); reload(); }
        })];
    case 'CHECKED_IN':
      return [button('Dịch vụ', '', () => openServices(b)), button('Trả phòng', 'primary', () => openCheckout(b, reload))];
    case 'CHECKED_OUT':
      return [button('Hóa đơn', '', () => openInvoice(b.id))];
    default:
      return [];
  }
}

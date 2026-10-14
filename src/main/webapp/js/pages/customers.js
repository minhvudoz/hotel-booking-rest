import { h, field, date, badge, money, modal, pager, toast, table } from '../ui.js';
import { api, guard, qs } from '../api.js';

export function customersPage(root) {
  const state = { q: '', page: 1 };
  const rows = h('tbody');
  const footer = h('div');
  const search = h('input', { placeholder: 'Tìm theo tên hoặc số điện thoại' });
  const go = () => { state.q = search.value.trim(); state.page = 1; load(); };
  search.addEventListener('keydown', e => { if (e.key === 'Enter') go(); });

  const load = guard(async () => {
    const page = await api('GET', '/customers' + qs(state));
    rows.replaceChildren(...page.items.map(c => h('tr', {},
      h('td', {}, c.fullName), h('td', {}, c.phone), h('td', {}, c.idCard ?? ''), h('td', {}, c.email ?? ''),
      h('td', { class: 'row-actions' },
        h('button', { class: 'btn', onclick: guard(() => history(c)) }, 'Lịch sử'),
        h('button', { class: 'btn ghost', onclick: () => edit(c, load) }, 'Sửa')))));
    footer.replaceChildren(pager(page, p => { state.page = p; load(); }));
  });

  root.append(h('h2', {}, 'Khách hàng'), h('div', { class: 'card' },
    h('div', { class: 'toolbar' }, search, h('button', { class: 'btn', onclick: go }, 'Tìm'),
      h('button', { class: 'btn primary', onclick: () => edit(null, load) }, '+ Thêm khách')),
    table(['Họ tên', 'Số điện thoại', 'CCCD', 'Email', ''], rows),
    footer));
  load();
}

function edit(customer, reload) {
  const inputs = {
    fullName: h('input', { value: customer?.fullName ?? '' }),
    phone: h('input', { value: customer?.phone ?? '' }),
    idCard: h('input', { value: customer?.idCard ?? '' }),
    email: h('input', { type: 'email', value: customer?.email ?? '' }),
  };
  const close = modal(customer ? 'Sửa thông tin khách' : 'Thêm khách hàng',
    h('div', {}, field('Họ tên *', inputs.fullName), field('Số điện thoại *', inputs.phone), field('CCCD', inputs.idCard), field('Email', inputs.email)),
    [h('button', { class: 'btn primary', onclick: guard(async () => {
      const body = Object.fromEntries(Object.entries(inputs).map(([k, el]) => [k, el.value.trim() || null]));
      await (customer ? api('PUT', `/customers/${customer.id}`, body) : api('POST', '/customers', body));
      close();
      toast('Đã lưu thông tin khách', 'success');
      reload();
    }) }, 'Lưu')]);
}

async function history(customer) {
  const list = await api('GET', `/customers/${customer.id}/bookings`);
  modal(`Lịch sử lưu trú – ${customer.fullName}`, list.length
    ? table(['Mã', 'Phòng', 'Nhận – Trả', 'Tiền phòng', 'Trạng thái'], h('tbody', {}, list.map(b => h('tr', {},
      h('td', {}, '#' + b.id), h('td', {}, b.roomNumber), h('td', {}, `${date(b.checkIn)} – ${date(b.checkOut)}`),
      h('td', { class: 'num' }, money(b.roomTotal)), h('td', {}, badge(b.status))))))
    : h('p', {}, 'Khách chưa có lượt lưu trú nào.'));
}

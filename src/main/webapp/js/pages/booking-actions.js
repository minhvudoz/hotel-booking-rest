import { h, field, money, date, dateTime, modal, toast, table } from '../ui.js';
import { api, guard } from '../api.js';

export async function openServices(b) {
  const [amenities, used] = await Promise.all([api('GET', '/amenities'), api('GET', `/bookings/${b.id}/services`)]);
  const rows = h('tbody');
  const reload = async () => render(await api('GET', `/bookings/${b.id}/services`));
  const render = items => rows.replaceChildren(...(items.length ? items.map(s => h('tr', {},
    h('td', {}, s.amenityName), h('td', {}, `${s.quantity} ${s.unit}`), h('td', { class: 'num' }, money(s.amount)),
    h('td', { class: 'row-actions' }, h('button', { class: 'btn ghost', onclick: guard(async () => {
      await api('DELETE', `/bookings/${b.id}/services/${s.id}`);
      await reload();
    }) }, 'Xóa')))) : [h('tr', {}, h('td', { colspan: 4 }, 'Chưa dùng dịch vụ nào.'))]));
  const amenity = h('select', {}, amenities.map(a => h('option', { value: a.id }, `${a.name} – ${money(a.unitPrice)}/${a.unit}`)));
  const quantity = h('input', { type: 'number', min: 1, value: 1 });
  render(used);
  modal(`Dịch vụ – phòng ${b.roomNumber}, khách ${b.customerName}`, h('div', {},
    table(['Dịch vụ', 'Số lượng', 'Thành tiền', ''], rows),
    h('div', { class: 'toolbar' }, field('Dịch vụ', amenity), field('Số lượng', quantity),
      h('button', { class: 'btn primary', onclick: guard(async () => {
        await api('POST', `/bookings/${b.id}/services`, { amenityId: Number(amenity.value), quantity: Number(quantity.value) });
        toast('Đã ghi dịch vụ', 'success');
        await reload();
      }) }, 'Thêm dịch vụ'))));
}

export async function openCheckout(b, onDone) {
  const invoice = await api('GET', `/bookings/${b.id}/invoice`);
  const method = h('select', {}, h('option', { value: 'CASH' }, 'Tiền mặt'), h('option', { value: 'TRANSFER' }, 'Chuyển khoản'));
  const close = modal(`Trả phòng ${b.roomNumber}`, h('div', {}, invoiceView(invoice), field('Hình thức thanh toán', method)),
    [h('button', { class: 'btn primary', onclick: guard(async () => {
      const paid = await api('PUT', `/bookings/${b.id}/check-out`, { paymentMethod: method.value });
      close();
      toast('Trả phòng thành công', 'success');
      onDone();
      showInvoice(paid);
    }) }, `Xác nhận thu ${money(invoice.total)}`)]);
}

export async function openInvoice(id) {
  showInvoice(await api('GET', `/bookings/${id}/invoice`));
}

function showInvoice(invoice) {
  modal(`Hóa đơn – đơn #${invoice.booking.id}`, invoiceView(invoice),
    [h('button', { class: 'btn primary', onclick: () => window.print() }, '🖨️ In hóa đơn')]);
}

function invoiceView(inv) {
  const b = inv.booking;
  return h('div', { class: 'invoice' },
    h('p', {}, h('b', {}, b.customerName), ` · ${b.customerPhone}`, h('br'),
      `Phòng ${b.roomNumber} · ${date(b.checkIn)} → ${date(b.checkOut)} (${b.nights} đêm)`),
    h('table', {}, h('tbody', {},
      h('tr', {}, h('td', {}, `Tiền phòng (${b.nights} đêm)`), h('td', { class: 'num' }, money(inv.roomTotal))),
      inv.services.map(s => h('tr', {}, h('td', {}, `${s.amenityName} × ${s.quantity} ${s.unit}`), h('td', { class: 'num' }, money(s.amount)))),
      h('tr', { class: 'total' }, h('td', {}, 'Tổng cộng'), h('td', { class: 'num' }, money(inv.total))))),
    inv.payment
      ? h('p', { class: 'paid' }, `Đã thanh toán ${inv.payment.method === 'CASH' ? 'tiền mặt' : 'chuyển khoản'} lúc ${dateTime(inv.payment.paidAt)}`)
      : null);
}

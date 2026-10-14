import { h, field, money, date, modal, toast } from '../ui.js';
import { api, guard, qs } from '../api.js';

export function openBookingForm(room, checkIn, checkOut, onDone) {
  const nights = Math.round((new Date(checkOut) - new Date(checkIn)) / 864e5);
  const phone = h('input', { placeholder: 'VD: 0912345678' });
  const info = h('div');
  const fresh = { fullName: h('input'), idCard: h('input'), email: h('input', { type: 'email' }) };
  let customer = null;
  let looked = false;

  const lookup = guard(async () => {
    const found = await api('GET', '/customers' + qs({ q: phone.value.trim(), size: 20 }));
    customer = found.items.find(c => c.phone === phone.value.trim()) ?? null;
    looked = true;
    info.replaceChildren(customer
      ? h('p', {}, '✅ Khách quen: ', h('b', {}, customer.fullName), ` · CCCD ${customer.idCard ?? '—'}`)
      : h('div', {}, h('p', {}, 'Khách mới, vui lòng nhập thông tin:'),
        field('Họ tên *', fresh.fullName), field('CCCD', fresh.idCard), field('Email', fresh.email)));
  });

  const submit = guard(async () => {
    if (!looked) return toast('Nhập số điện thoại rồi bấm "Tìm khách" trước');
    customer ??= await api('POST', '/customers', {
      phone: phone.value.trim(), fullName: fresh.fullName.value.trim(),
      idCard: fresh.idCard.value.trim() || null, email: fresh.email.value.trim() || null,
    });
    const booking = await api('POST', '/bookings', { roomId: room.id, customerId: customer.id, checkIn, checkOut });
    close();
    toast(`Đã đặt phòng ${booking.roomNumber} cho ${booking.customerName} (mã đơn #${booking.id})`, 'success');
    onDone();
  });

  const close = modal(`Đặt phòng ${room.roomNumber}`,
    h('div', {},
      h('p', {}, `${date(checkIn)} → ${date(checkOut)} · ${nights} đêm · Tiền phòng `, h('b', {}, money(nights * room.pricePerNight))),
      h('div', { class: 'toolbar' }, field('Số điện thoại khách *', phone), h('button', { class: 'btn', onclick: lookup }, 'Tìm khách')),
      info),
    [h('button', { class: 'btn primary', onclick: submit }, 'Xác nhận đặt phòng')]);
}

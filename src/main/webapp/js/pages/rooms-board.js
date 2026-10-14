import { h, field, money, badge, ROOM_TYPES, today } from '../ui.js';
import { api, guard, qs } from '../api.js';
import { openBookingForm } from './booking-form.js';

export function roomsBoardPage(root) {
  const grid = h('div', { class: 'room-grid' });
  const results = h('div', { class: 'room-grid' });
  const day = h('input', { type: 'date', value: today(), onchange: () => loadStatus() });
  const checkIn = h('input', { type: 'date', value: today(), min: today() });
  const checkOut = h('input', { type: 'date', value: today(1), min: today(1) });
  const type = h('select', {}, h('option', { value: '' }, 'Tất cả loại'),
    Object.entries(ROOM_TYPES).map(([value, label]) => h('option', { value }, label)));

  const loadStatus = guard(async () => {
    const rooms = await api('GET', '/rooms/status' + qs({ date: day.value }));
    grid.replaceChildren(...rooms.map(s => h('div', { class: `room ${s.status.toLowerCase()}` },
      h('b', {}, s.room.roomNumber), h('small', {}, ROOM_TYPES[s.room.type]), badge(s.status),
      s.guestName ? h('small', {}, s.guestName) : null)));
  });

  const findAvailable = guard(async () => {
    const rooms = await api('GET', '/rooms/available' + qs({ checkIn: checkIn.value, checkOut: checkOut.value, type: type.value }));
    results.replaceChildren(...(rooms.length ? rooms.map(r => h('div', { class: 'room available' },
      h('b', {}, r.roomNumber),
      h('small', {}, `${ROOM_TYPES[r.type]} · ${r.capacity} khách`),
      h('small', {}, money(r.pricePerNight) + ' / đêm'),
      h('button', { class: 'btn primary', onclick: () => openBookingForm(r, checkIn.value, checkOut.value, () => { loadStatus(); findAvailable(); }) }, 'Đặt phòng')))
      : [h('p', {}, 'Không còn phòng trống trong khoảng ngày này.')]));
  });

  root.append(
    h('h2', {}, 'Sơ đồ phòng'),
    h('div', { class: 'card' },
      h('div', { class: 'toolbar' }, field('Xem tình trạng ngày', day),
        h('div', { class: 'legend' }, badge('AVAILABLE'), badge('BOOKED'), badge('OCCUPIED'))),
      grid),
    h('div', { class: 'card' },
      h('h3', {}, 'Tìm phòng trống để đặt'),
      h('div', { class: 'toolbar' }, field('Ngày nhận', checkIn), field('Ngày trả', checkOut), field('Loại phòng', type),
        h('button', { class: 'btn primary', onclick: findAvailable }, 'Tìm phòng trống')),
      results));
  loadStatus();
}

import { h, field, money, modal, toast, table, ROOM_TYPES } from '../ui.js';
import { api, guard, qs } from '../api.js';

const ROOMS = {
  title: 'Phòng', path: '/rooms',
  list: async () => (await api('GET', '/rooms' + qs({ size: 100 }))).items,
  columns: [['Số phòng', r => r.roomNumber], ['Loại', r => ROOM_TYPES[r.type]], ['Giá / đêm', r => money(r.pricePerNight)],
    ['Sức chứa', r => r.capacity], ['Mô tả', r => r.description ?? '']],
  fields: [['roomNumber', 'Số phòng *'], ['type', 'Loại phòng *', ROOM_TYPES], ['pricePerNight', 'Giá / đêm (VNĐ) *', 'number'],
    ['capacity', 'Sức chứa *', 'number'], ['description', 'Mô tả']],
};

const AMENITIES = {
  title: 'Dịch vụ', path: '/amenities',
  list: () => api('GET', '/amenities'),
  columns: [['Tên dịch vụ', a => a.name], ['Đơn giá', a => money(a.unitPrice)], ['Đơn vị', a => a.unit]],
  fields: [['name', 'Tên dịch vụ *'], ['unitPrice', 'Đơn giá (VNĐ) *', 'number'], ['unit', 'Đơn vị tính *']],
};

export function adminPage(root) {
  root.append(h('h2', {}, 'Quản trị'), crud(ROOMS), crud(AMENITIES));
}

function crud(cfg) {
  const rows = h('tbody');
  const load = guard(async () => {
    const items = await cfg.list();
    rows.replaceChildren(...items.map(item => h('tr', {},
      cfg.columns.map(([, get]) => h('td', {}, get(item))),
      h('td', { class: 'row-actions' },
        h('button', { class: 'btn', onclick: () => edit(cfg, item, load) }, 'Sửa'),
        h('button', { class: 'btn ghost', onclick: guard(async () => {
          if (!confirm(`Xóa ${cfg.title.toLowerCase()} này?`)) return;
          await api('DELETE', `${cfg.path}/${item.id}`);
          toast('Đã xóa', 'success');
          load();
        }) }, 'Xóa')))));
  });
  load();
  return h('div', { class: 'card' },
    h('div', { class: 'toolbar' }, h('h3', {}, cfg.title),
      h('button', { class: 'btn primary', onclick: () => edit(cfg, null, load) }, `+ Thêm ${cfg.title.toLowerCase()}`)),
    table([...cfg.columns.map(([title]) => title), ''], rows));
}

function edit(cfg, item, reload) {
  const inputs = Object.fromEntries(cfg.fields.map(([name, , kind]) => [name, typeof kind === 'object'
    ? h('select', {}, Object.entries(kind).map(([value, label]) => h('option', { value, selected: item?.[name] === value }, label)))
    : h('input', { type: kind ?? 'text', value: item?.[name] ?? '' })]));
  const close = modal(`${item ? 'Sửa' : 'Thêm'} ${cfg.title.toLowerCase()}`,
    h('div', {}, cfg.fields.map(([name, label]) => field(label, inputs[name]))),
    [h('button', { class: 'btn primary', onclick: guard(async () => {
      const body = Object.fromEntries(cfg.fields.map(([name, , kind]) =>
        [name, kind === 'number' ? Number(inputs[name].value) : inputs[name].value.trim() || null]));
      await (item ? api('PUT', `${cfg.path}/${item.id}`, body) : api('POST', cfg.path, body));
      close();
      toast('Đã lưu', 'success');
      reload();
    }) }, 'Lưu')]);
}

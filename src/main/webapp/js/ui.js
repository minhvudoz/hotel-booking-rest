export function h(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs ?? {})) {
    if (value == null || value === false) continue;
    if (key.startsWith('on')) node.addEventListener(key.slice(2).toLowerCase(), value);
    else if (key === 'class') node.className = value;
    else if (key === 'value') node.value = value;
    else node.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat()) {
    if (child != null && child !== false) node.append(child instanceof Node ? child : String(child));
  }
  return node;
}

export const money = n => new Intl.NumberFormat('vi-VN').format(n) + ' ₫';
export const date = s => (s ? s.split('-').reverse().join('/') : '');
export const dateTime = s => (s ? new Date(s).toLocaleString('vi-VN') : '');
export function today(offset = 0) {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  return d.toLocaleDateString('sv-SE');
}

export const STATUS = {
  BOOKED: ['Đã đặt', 'warn'], CHECKED_IN: ['Đang ở', 'danger'], CHECKED_OUT: ['Đã trả phòng', ''],
  CANCELLED: ['Đã hủy', ''], AVAILABLE: ['Trống', 'ok'], OCCUPIED: ['Có khách', 'danger'],
};
export const badge = s => h('span', { class: `badge ${STATUS[s]?.[1] ?? ''}` }, STATUS[s]?.[0] ?? s);
export const ROOM_TYPES = { STANDARD: 'Standard', DELUXE: 'Deluxe', SUITE: 'Suite' };

export function toast(message, kind = 'error') {
  const node = h('div', { class: `toast ${kind}` }, message);
  document.getElementById('toasts').append(node);
  setTimeout(() => node.remove(), 4500);
}

export function modal(title, body, actions = []) {
  const close = () => overlay.remove();
  const overlay = h('div', { class: 'overlay', onclick: e => { if (e.target === overlay) close(); } },
    h('div', { class: 'modal' }, h('h3', {}, title), body,
      h('div', { class: 'actions' }, h('button', { class: 'btn ghost', onclick: close }, 'Đóng'), ...actions)));
  document.body.append(overlay);
  return close;
}

export const field = (label, input) => h('label', { class: 'field' }, h('span', {}, label), input);
export const table = (headers, body) => h('table', {}, h('thead', {}, h('tr', {}, headers.map(t => h('th', {}, t)))), body);

export function pager(page, onGo) {
  const pages = Math.max(1, Math.ceil(page.total / page.size));
  return h('div', { class: 'pager' },
    h('button', { class: 'btn ghost', disabled: page.page <= 1, onclick: () => onGo(page.page - 1) }, '‹ Trang trước'),
    h('span', {}, `Trang ${page.page}/${pages} · ${page.total} mục`),
    h('button', { class: 'btn ghost', disabled: page.page >= pages, onclick: () => onGo(page.page + 1) }, 'Trang sau ›'));
}

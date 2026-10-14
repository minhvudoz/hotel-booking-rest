import { h } from './ui.js';
import { session } from './api.js';
import { loginPage } from './pages/login.js';
import { dashboardPage } from './pages/dashboard.js';

const ROUTES = [
  { path: '#/dashboard', label: '📊 Tổng quan', page: dashboardPage, roles: ['ADMIN'] },
];

const home = user => ROUTES.find(r => r.roles.includes(user.role)).path;

function render() {
  const app = document.getElementById('app');
  const current = session.get();
  if (!current) {
    app.replaceChildren(loginPage(() => {
      const target = home(session.get().user);
      if (location.hash === target) render(); else location.hash = target;
    }));
    return;
  }
  const allowed = ROUTES.filter(r => r.roles.includes(current.user.role));
  const route = allowed.find(r => r.path === location.hash) ?? allowed[0];
  if (location.hash !== route.path) {
    location.hash = route.path;
    return;
  }
  const content = h('main', { class: 'content' });
  app.replaceChildren(h('div', { class: 'layout' },
    h('aside', { class: 'sidebar' },
      h('div', { class: 'brand' }, '🏨 Khách sạn'),
      h('nav', {},
        allowed.map(r => h('a', { href: r.path, class: r === route ? 'active' : '' }, r.label)),
        h('a', { href: 'docs/', target: '_blank' }, '📘 Tài liệu API')),
      h('div', { class: 'user' },
        h('div', {}, current.user.fullName),
        h('small', {}, current.user.role === 'ADMIN' ? 'Quản trị viên' : 'Lễ tân'),
        h('button', { class: 'btn ghost', onclick: () => { session.clear(); render(); } }, 'Đăng xuất'))),
    content));
  route.page(content);
}

window.addEventListener('hashchange', render);
render();

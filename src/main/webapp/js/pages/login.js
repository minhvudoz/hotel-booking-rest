import { h, field } from '../ui.js';
import { api, guard, session } from '../api.js';

export function loginPage(onSuccess) {
  const username = h('input', { name: 'username', required: true, autocomplete: 'username' });
  const password = h('input', { name: 'password', type: 'password', required: true, autocomplete: 'current-password' });
  const submit = guard(async e => {
    e.preventDefault();
    session.set(await api('POST', '/auth/login', { username: username.value.trim(), password: password.value }));
    onSuccess();
  });
  return h('div', { class: 'login-wrap' },
    h('form', { class: 'card login', onsubmit: submit },
      h('h1', {}, '🏨 Quản lý Khách sạn'),
      field('Tên đăng nhập', username),
      field('Mật khẩu', password),
      h('button', { class: 'btn primary', type: 'submit' }, 'Đăng nhập'),
      h('div', { class: 'hint' },
        'Tài khoản dùng thử:', h('br'),
        h('b', {}, 'admin / admin123'), ' – quản trị viên', h('br'),
        h('b', {}, 'letan / letan123'), ' – lễ tân')));
}

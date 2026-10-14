import { toast } from './ui.js';

const KEY = 'hotel.session';

export const session = {
  get() { try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; } },
  set(value) { try { localStorage.setItem(KEY, JSON.stringify(value)); } catch { /* trình duyệt chặn lưu trữ */ } },
  clear() { try { localStorage.removeItem(KEY); } catch { /* trình duyệt chặn lưu trữ */ } },
};

export async function api(method, path, body) {
  const current = session.get();
  const res = await fetch('api' + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(current ? { Authorization: 'Bearer ' + current.token } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (res.status === 401 && current) {
    session.clear();
    location.reload();
  }
  if (!res.ok) {
    const message = data?.error ?? `Lỗi ${res.status}`;
    toast(message);
    throw new Error(message);
  }
  return data;
}

// Lỗi đã được hiện bằng toast trong api(), nên ở đây chỉ chặn unhandled rejection
export const guard = fn => async (...args) => { try { return await fn(...args); } catch { return undefined; } };

export const qs = params => {
  const query = new URLSearchParams(Object.entries(params).filter(([, v]) => v !== '' && v != null)).toString();
  return query ? '?' + query : '';
};

export const variables = { baseUrl: 'http://localhost:8080', adminToken: '', letanToken: '' };

// Ngày theo giờ Việt Nam (UTC+7) để khớp với Env.today() của server
export const collectionPrerequest = [
  "const vnDay = n => new Date(Date.now() + 7 * 3600e3 + n * 864e5).toISOString().slice(0, 10);",
  "[['yesterday', -1], ['today', 0], ['tomorrow', 1], ['future1', 30], ['future2', 32]].forEach(([k, n]) => pm.collectionVariables.set(k, vnDay(n)));",
];

const AUTH = { admin: 'Bearer {{adminToken}}', letan: 'Bearer {{letanToken}}', invalid: 'Bearer invalid.token.value' };

export const folder = (name, item) => ({ name, item });

export function req(name, method, path, { auth = 'admin', body, tests = [], pre = [] } = {}) {
  const header = [];
  if (auth) header.push({ key: 'Authorization', value: AUTH[auth] });
  if (body !== undefined) header.push({ key: 'Content-Type', value: 'application/json' });
  const event = [{ listen: 'test', script: { type: 'text/javascript', exec: tests.flat() } }];
  if (pre.length) event.unshift({ listen: 'prerequest', script: { type: 'text/javascript', exec: pre } });
  const request = { method, header, url: `{{baseUrl}}/api${path}` };
  if (body !== undefined) {
    request.body = { mode: 'raw', raw: typeof body === 'string' ? body : JSON.stringify(body, null, 2), options: { raw: { language: 'json' } } };
  }
  return { name, event, request };
}

export const status = code => `pm.test('HTTP ${code}', () => pm.response.to.have.status(${code}));`;
export const check = (title, expr) =>
  `pm.test(${JSON.stringify(title)}, () => { const body = pm.response.json(); pm.expect(${expr}, ${JSON.stringify(expr)}).to.be.true; });`;
export const save = (name, expr) =>
  `if (pm.response.code < 300) { const body = pm.response.json(); pm.collectionVariables.set('${name}', ${expr}); }`;
export const error = code => [status(code), check('Có thông điệp lỗi', "typeof body.error === 'string' && body.error.length > 0")];
export const v = name => `pm.collectionVariables.get('${name}')`;

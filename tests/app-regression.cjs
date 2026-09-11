const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const { test } = require('node:test');
const path = require('node:path');

const html = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/index.html'), 'utf8');
function harness() {
  const storage = new Map();
  const alerts = [];
  let options;
  const context = vm.createContext({
    window: { addEventListener: () => {} }, console, Date, setTimeout, clearTimeout,
    localStorage: {
      getItem: key => storage.get(key) ?? null,
      setItem: (key, value) => storage.set(key, value),
      removeItem: key => storage.delete(key),
    },
    alert: message => alerts.push(message),
    fetch: async () => { throw new Error('Unexpected network request'); },
    Vue: { createApp: value => { options = value; return { mount: () => ({}) }; } },
  });
  for (const match of html.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi)) {
    if (!/\bsrc\s*=/.test(match[1])) vm.runInContext(match[2], context);
  }
  const app = options.data();
  for (const [key, method] of Object.entries(options.methods)) app[key] = method.bind(app);
  for (const [key, getter] of Object.entries(options.computed)) Object.defineProperty(app, key, { get: getter.bind(app) });
  return { app, context, storage, alerts, mount: () => options.mounted.call(app) };
}

test('empty username and password are rejected before contacting server', async () => {
  const h = harness();
  h.app.loginData = { username: '', password: '' };
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Nama Pengguna/);
  h.app.loginData.username = 'admin';
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Kata Sandi/);
  assert.equal(h.storage.size, 0);
});

test('incorrect credentials and connection failures do not create a session', async () => {
  const h = harness();
  h.app.view = 'login';
  h.app.loginData = { username: 'admin', password: 'incorrect' };
  h.context.fetch = async () => ({ ok: false, json: async () => ({ msg: 'Invalid credentials' }) });
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Verifikasi Database Gagal/);
  assert.equal(h.app.view, 'login');
  assert.equal(h.app.isLoggingIn, false);
  h.context.fetch = async () => { throw new Error('offline'); };
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Koneksi Terputus/);
  assert.equal(h.app.isLoggingIn, false);
  assert.equal(h.storage.has('SIMPATIK_SESSION'), false);
});

for (const role of ['admin', 'kader']) test(`${role}: login, session restore, and logout`, async () => {
  const h = harness();
  h.app.loginData = { username: ` ${role.toUpperCase()} `, password: 'test-password' };
  h.context.fetch = async (url, request) => {
    assert.ok(url.endsWith('/auth/v1/token?grant_type=password'));
    assert.equal(JSON.parse(request.body).email, `${role}@posyandu.id`);
    return { ok: true, json: async () => ({ access_token: 'test-only', user: { user_metadata: { role, name: 'Test' } } }) };
  };
  await h.app.doLogin();
  assert.equal(h.app.currentUser.role, role);
  assert.equal(h.app.view, 'home');
  assert.equal(h.app.loginData.password, '');
  assert.equal(h.app.isLoggingIn, false);
  h.app.registryConfig.activeProvider = 'local';
  h.app.view = 'login';
  h.mount();
  assert.equal(h.app.view, 'home');
  h.app.doLogout();
  assert.equal(h.app.view, 'login');
  assert.equal(h.app.currentUser, null);
  assert.equal(h.storage.has('SIMPATIK_SESSION'), false);
  assert.equal(h.storage.has('SIMPATIK_CURRENT_USER'), false);
});

test('Back closes dialogs before changing screens', () => {
  const { app } = harness();
  app.view = 'form';
  app.bukaDialog = true;
  assert.equal(app.handleAndroidBack(), 'handled');
  assert.equal(app.bukaDialog, false);
  assert.equal(app.view, 'form');
});

test('Back follows navigation routes and delegates root exit to Android', () => {
  const { app } = harness();
  for (const [from, to] of [['form', 'list'], ['list', 'home']]) {
    app.view = from;
    assert.equal(app.handleAndroidBack(), 'handled');
    assert.equal(app.view, to);
  }
  for (const root of ['login', 'home']) {
    app.view = root;
    assert.equal(app.handleAndroidBack(), 'unhandled');
    assert.equal(app.view, root);
  }
});

test('measurement validation rejects incomplete and implausible values', () => {
  const { app, alerts } = harness();
  app.activeAnak = { id: '3273010101220001', nik: '3273010101220001', tglLahir: '2022-01-01', riwayat: {} };
  app.tanggalUkur = '2026-09-10';
  app.formVals = { bb: '', pb: '', lila: '', lika: '' };
  assert.equal(app.siapkanKonfirmasiSimpan(), false);
  assert.match(alerts.pop(), /Berat badan wajib/);
  app.formVals = { bb: '85', pb: '75', lila: '14', lika: '45' };
  assert.equal(app.siapkanKonfirmasiSimpan(), false);
  assert.match(alerts.pop(), /luar rentang wajar/);
  app.formVals.bb = '8,5';
  assert.equal(app.siapkanKonfirmasiSimpan(), true);
  assert.equal(app.bukaDialog, true);
});

test('saving twice in one child-period updates one stable record', async () => {
  const h = harness();
  const nativeRows = new Map();
  let revision = 0;
  h.context.window.AndroidBridge = {
    simpanDataPengukuran: json => {
      const payload = JSON.parse(json);
      revision++;
      const action = nativeRows.has(payload.id_pengukuran) ? 'updated' : 'inserted';
      nativeRows.set(payload.id_pengukuran, payload);
      return JSON.stringify({ ok: true, action, revision });
    },
    simpanDaftarAnak: () => JSON.stringify({ ok: true }),
    tampilkanPesan: () => {},
  };
  h.app.registryConfig.activeProvider = 'json_offline';
  const child = { id: '3273010101220001', nik: '3273010101220001', tglLahir: '2022-01-01', riwayat: {}, sudahDiukur: false };
  h.app.daftarAnak = [child];
  h.app.view = 'list';
  h.app.bukaFormPengukuran(child, '2026-09-10');
  h.app.formVals = { bb: '8,5', pb: '75,1', lila: '14,2', lika: '45' };
  await h.app.simpanData();
  const firstId = child.riwayat['2026'][8].idPengukuran;
  assert.equal(nativeRows.size, 1);
  h.app.bukaFormPengukuran(child, '2026-09-09');
  assert.equal(h.app.isEditingRecord, true);
  assert.equal(h.app.tanggalUkur, '2026-09-10');
  h.app.formVals.bb = '8,7';
  await h.app.simpanData();
  assert.equal(nativeRows.size, 1);
  assert.equal(child.riwayat['2026'][8].idPengukuran, firstId);
  assert.equal(child.riwayat['2026'][8].bb, '8.7');
  assert.equal(child.riwayat['2026'][8].revision, 2);
  h.app.bukaFormPengukuran(child, '2026-09-09');
  h.app.tanggalUkur = '2026-08-28';
  await h.app.simpanData();
  assert.equal(nativeRows.size, 1);
  assert.equal(child.riwayat['2026'][8], undefined);
  assert.equal(child.riwayat['2026'][7].idPengukuran, firstId);
  assert.equal(child.riwayat['2026'][7].revision, 3);
});

test('cloud sync uses upsert and marks a queued record as synced', async () => {
  const h = harness();
  const marked = [];
  h.context.window.AndroidBridge = {
    ambilPengukuranTertunda: () => JSON.stringify([{ id_pengukuran: 'ukur_1_202609', nik: '1', id_periode: 'periode_2026_9', tanggal_ukur: '2026-09-10' }]),
    tandaiPengukuranTersinkron: id => { marked.push(id); return true; },
  };
  h.app.registryConfig.activeProvider = 'supabase_cloud';
  h.context.fetch = async (url, request) => {
    assert.match(url, /on_conflict=id_pengukuran/);
    assert.match(request.headers.Prefer, /merge-duplicates/);
    return { ok: true, status: 201 };
  };
  assert.equal(await h.app.sinkronkanDataTertunda(), 0);
  assert.deepEqual(marked, ['ukur_1_202609']);
});

test('failed cloud upload remains queued for automatic retry', async () => {
  const h = harness();
  const payload = { id_pengukuran: 'ukur_2_202609', nik: '2', id_periode: 'periode_2026_9', tanggal_ukur: '2026-09-10' };
  h.context.window.AndroidBridge = {
    ambilPengukuranTertunda: () => JSON.stringify([payload]),
    tandaiPengukuranTersinkron: () => { throw new Error('must not be called'); },
  };
  h.app.registryConfig.activeProvider = 'supabase_cloud';
  h.context.fetch = async () => ({ ok: false, status: 503 });
  assert.equal(await h.app.sinkronkanDataTertunda(), 1);
  assert.equal(h.app.pendingSyncCount, 1);
});

test('tablet UI exposes field recording without portal analytics modules', () => {
  assert.match(html, /Pencatatan Lapangan/);
  assert.match(html, /Mulai Pencatatan/);
  assert.match(html, /Progres pencatatan bulan ini/);
  assert.doesNotMatch(html, /view === 'history'/);
  assert.doesNotMatch(html, /view === 'nutrition_detail'/);
  assert.doesNotMatch(html, /view === 'settings'/);
  assert.doesNotMatch(html, /view === 'register'/);
  assert.doesNotMatch(html, /kartuHasil\(\)/);
  assert.doesNotMatch(html, /hasilSimulasiZScore\(\)/);
});

const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const { test } = require('node:test');
const path = require('node:path');

const html = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/index.html'), 'utf8');
const portalApiBawaan = html.match(/const PORTAL_API_BAWAAN = '([^']+)'/)[1];
function harness(initialStorage = {}) {
  const storage = new Map(Object.entries(initialStorage));
  const alerts = [];
  let options;
  const context = vm.createContext({
    window: { addEventListener: () => {} }, console, Date, URL, setTimeout, clearTimeout,
    setInterval: () => 1, clearInterval: () => {}, AbortController,
    localStorage: {
      getItem: key => storage.get(key) ?? null,
      setItem: (key, value) => storage.set(key, value),
      removeItem: key => storage.delete(key),
    },
    alert: message => alerts.push(message), confirm: () => true,
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
  h.context.fetch = async () => ({ ok: false, status: 401, json: async () => ({ msg: 'Invalid credentials' }) });
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Verifikasi Login Gagal/);
  assert.equal(h.app.view, 'login');
  assert.equal(h.app.isLoggingIn, false);
  h.context.fetch = async () => { throw new Error('offline'); };
  await h.app.doLogin();
  assert.match(h.alerts.pop(), /Koneksi Terputus/);
  assert.equal(h.app.isLoggingIn, false);
  assert.equal(h.storage.has('SIMPATIK_SESSION'), false);
});

test('login role is assigned by the authenticated server account', () => {
  assert.doesNotMatch(html, /loginRole/);
  assert.doesNotMatch(html, /Masuk sebagai/);
  assert.match(html, /Hak akses diterapkan otomatis sesuai klasifikasi akun di server/);
  assert.match(html, /autocomplete="username"/);
  assert.match(html, /autocomplete="current-password"/);
});

test('password visibility toggles without changing its value and resets on logout', () => {
  const h = harness();
  h.app.loginData.password = ' contoh kata sandi ';
  assert.equal(h.app.showLoginPassword, false);
  h.app.toggleLoginPassword();
  assert.equal(h.app.showLoginPassword, true);
  assert.equal(h.app.loginData.password, ' contoh kata sandi ');
  h.app.toggleLoginPassword();
  assert.equal(h.app.showLoginPassword, false);
  h.app.toggleLoginPassword();
  h.app.doLogout();
  assert.equal(h.app.showLoginPassword, false);
  assert.equal(h.app.loginData.password, '');
});

test('login preserves password spaces and identifies each app installation', async () => {
  const h = harness();
  let request;
  h.app.loginData = { username: 'Kader.01', password: ' rahasia dengan spasi ' };
  h.context.fetch = async (url, options) => {
    request = { url, options };
    return { ok: true, json: async () => ({
      token: 'token-tablet', pengguna: { id: 9, nama: 'Kader', username: 'kader.01', peran: 'kader', rt: null },
    }) };
  };
  const deviceId = h.app.idPerangkatLogin();
  assert.equal(h.app.idPerangkatLogin(), deviceId);
  await h.app.doLogin();
  assert.equal(JSON.parse(request.options.body).kataSandi, ' rahasia dengan spasi ');
  assert.equal(request.options.headers['X-SIMpatik-Device-Id'], deviceId);
});

test('online tunnel profile uses one fixed HTTPS API and verifies database health', async () => {
  const h = harness();
  assert.equal(h.app.registryConfig.portalApi.url, portalApiBawaan);
  assert.match(portalApiBawaan, /^https:\/\/[a-zA-Z0-9.-]+\.(?:trycloudflare\.com|cloudflare\.com|ngrok-free\.dev|ngrok-free\.app|ngrok\.app)\/api\/v1$/);
  h.context.fetch = async (url, request) => {
    assert.equal(url, portalApiBawaan + '/kesehatan');
    return { ok: true, json: async () => ({ status: 'siap', database: 'terhubung' }) };
  };
  await h.app.ujiKoneksiPortal();
  assert.equal(h.app.registryConfig.portalApi.status, 'API & database siap');
  assert.match(h.alerts.pop(), /Koneksi Berhasil/);
});

test('connection label follows the actual API URL despite a stale local profile', () => {
  const h = harness();
  h.app.registryConfig.connectionMode = 'lokal';
  h.app.registryConfig.portalApi.url = portalApiBawaan;
  assert.equal(h.app.koneksiPortal.label, 'online');
  for (const host of ['127.0.0.1', '10.0.2.2', '192.168.1.9', '172.31.5.4', '[::1]']) {
    assert.equal(h.app.informasiKoneksiPortal('http://' + host + ':4321/api/v1').mode, 'lokal');
  }
  assert.equal(h.app.informasiKoneksiPortal('https://simpatik.tail123.ts.net/api/v1').label, 'Tailscale');
  assert.equal(h.app.informasiKoneksiPortal('https://172.32.1.2/api/v1').label, 'online');
});

test('discovery of an online URL does not announce a local connection', async () => {
  const h = harness();
  h.app.ujiKoneksiPortal = async () => true;
  h.app.kirimHeartbeat = async () => true;
  h.context.window.AndroidBridge = {
    temukanServer: () => h.context.window.handleServerDitemukan({ apiUrl: portalApiBawaan }),
  };
  assert.equal(await h.app.mulaiPenemuanLokal({ senyap: false }), true);
  assert.equal(h.app.registryConfig.connectionMode, 'online');
  assert.match(h.app.notice.message, /server online/);
  assert.doesNotMatch(h.app.notice.message, /lokal/);
});

test('device heartbeat is authenticated and does not claim an account or RW from client fields', async () => {
  const h = harness();
  h.app.registryConfig.portalApi.status = 'API & database siap';
  h.context.fetch = async () => { throw Error('No heartbeat before login'); };
  assert.equal(await h.app.kirimHeartbeat(), false);
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'token-uji-perangkat' }));
  let body;
  h.context.fetch = async (url, request) => {
    assert.equal(url, portalApiBawaan + '/heartbeat');
    assert.equal(request.headers.Authorization, 'Bearer token-uji-perangkat');
    body = JSON.parse(request.body);
    return { ok: true };
  };
  assert.equal(await h.app.kirimHeartbeat(), true);
  assert.equal(body.jenisPerangkat, 'android');
  assert.equal(body.modeKoneksi, 'online');
  assert.equal(body.versiAplikasi, 'v1.12.6');
  assert.equal(body.posyanduId, undefined);
  assert.equal(body.penggunaId, undefined);
});

test('splash checks the server only for a saved session and keeps offline data available', async () => {
  const fresh = harness();
  await fresh.app.inisialisasiKoneksiAwal();
  assert.equal(fresh.app.view, 'login');
  assert.equal(fresh.app.splashStatus, '');

  const returning = harness();
  returning.storage.set('SIMPATIK_CURRENT_USER', JSON.stringify({ username: 'kader', name: 'Kader', role: 'kader' }));
  returning.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'saved-token' }));
  returning.app.$nextTick = callback => callback();
  returning.app.ujiKoneksiPortal = async () => false;
  await returning.app.inisialisasiKoneksiAwal();
  assert.equal(returning.app.view, 'home');
  assert.equal(returning.app.showSplash, false);
  assert.equal(returning.app.notice.type, 'warning');
  assert.match(returning.app.notice.message, /Data tetap dapat dicatat/);

  assert.match(html, /Menyambungkan ke server/);
  assert.doesNotMatch(html, /Koneksi Server PC/);
  assert.doesNotMatch(html, /Opsi alamat IP manual/);
});

test('splash rejects a stale local session before showing cached monthly targets', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_CURRENT_USER', JSON.stringify({ username: 'admin', name: 'Admin', role: 'admin' }));
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'stale-token' }));
  h.app.currentUser = { username: 'admin', name: 'Admin', role: 'admin' };
  h.app.$nextTick = callback => callback();
  h.app.ujiKoneksiPortal = async () => true;
  h.app.validasiSesiPortal = async () => {
    h.app.akhiriSesiPortalTidakBerlaku();
    return 'invalid';
  };

  await h.app.inisialisasiKoneksiAwal();

  assert.equal(h.app.view, 'login');
  assert.equal(h.app.currentUser, null);
  assert.equal(h.storage.has('SIMPATIK_SESSION'), false);
  assert.equal(h.storage.has('SIMPATIK_CURRENT_USER'), false);
  assert.match(h.app.notice.message, /masuk kembali/);
});

for (const role of ['admin', 'kader']) test(`${role}: login, session restore, and logout`, async () => {
  const h = harness();
  h.app.loginData = { username: ` ${role.toUpperCase()} `, password: 'test-password' };
  h.context.fetch = async (url, request) => {
    assert.ok(url.endsWith('/masuk'));
    assert.equal(JSON.parse(request.body).username, role);
    return { ok: true, json: async () => ({ token: 'test-only', pengguna: { username: role, peran: role, nama: 'Test', rt: role === 'kader' ? '01' : null } }) };
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
  for (const [from, to] of [['form', 'list'], ['list', 'home'], ['register', 'home'], ['queue', 'home']]) {
    app.view = from;
    assert.equal(app.handleAndroidBack(), 'handled');
    assert.equal(app.view, to);
  }
  app.activeAnak = { riwayat: {} };
  app.previousView = 'list';
  app.view = 'history';
  assert.equal(app.handleAndroidBack(), 'handled');
  assert.equal(app.view, 'list');
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
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'user-token' }));
  const marked = [];
  h.context.window.AndroidBridge = {
    ambilPengukuranTertunda: () => marked.length ? '[]' : JSON.stringify([{ id_pengukuran: 'ukur_1_202609', nik: '1', id_periode: 'periode_2026_9', tanggal_ukur: '2026-09-10' }]),
    tandaiPengukuranTersinkron: id => { marked.push(id); return true; },
  };
  h.app.registryConfig.activeProvider = 'portal_api';
  h.context.fetch = async (url, request) => {
    assert.match(url, /\/api\/v1\/pengukuran/);
    assert.equal(request.headers.Authorization, 'Bearer user-token');
    assert.equal(JSON.parse(request.body).id_pengukuran, 'ukur_1_202609');
    return { ok: true, status: 201 };
  };
  assert.equal(await h.app.sinkronkanDataTertunda(), 0);
  assert.deepEqual(marked, ['ukur_1_202609']);
});

test('failed cloud upload remains queued for automatic retry', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'user-token' }));
  const payload = { id_pengukuran: 'ukur_2_202609', nik: '2', id_periode: 'periode_2026_9', tanggal_ukur: '2026-09-10' };
  h.context.window.AndroidBridge = {
    ambilPengukuranTertunda: () => JSON.stringify([payload]),
    tandaiPengukuranTersinkron: () => { throw new Error('must not be called'); },
  };
  h.app.registryConfig.activeProvider = 'portal_api';
  h.context.fetch = async () => ({ ok: false, status: 503 });
  assert.equal(await h.app.sinkronkanDataTertunda(), 1);
  assert.equal(h.app.pendingSyncCount, 1);
});

test('child registration is stored locally and duplicate NIK is rejected', async () => {
  const h = harness();
  h.app.registryConfig.activeProvider = 'json_offline';
  h.app.daftarAnak = [];
  h.app.regData = { nik: '3273010101220001', nama: 'Anak Uji', tglLahir: '2022-01-01', jk: 'L', namaIbu: 'Ibu Uji', rt: '01' };
  await h.app.simpanRegistrasi();
  assert.equal(h.app.daftarAnak.length, 1);
  assert.equal(h.app.daftarAnak[0].syncStatus, 'pending');
  assert.equal(h.app.view, 'home');
  h.app.regData = { nik: '3273010101220001', nama: 'Duplikat', tglLahir: '2022-01-01', jk: 'L', namaIbu: 'Ibu Uji', rt: '01' };
  await h.app.simpanRegistrasi();
  assert.equal(h.app.daftarAnak.length, 1);
  assert.match(h.alerts.pop(), /Duplikat Dicegah/);
});

test('pending child registration is upserted and marked as synced', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'user-token' }));
  const child = { id: '3273010101220002', nik: '3273010101220002', nama: 'Anak Server', tglLahir: '2023-02-01', jk: 'P', namaOrtu: 'Ibu Server', rt: '02', syncStatus: 'pending', riwayat: {} };
  h.app.daftarAnak = [child];
  h.app.registryConfig.activeProvider = 'portal_api';
  h.context.fetch = async (url, request) => {
    assert.match(url, /\/api\/v1\/anak/);
    assert.equal(request.headers.Authorization, 'Bearer user-token');
    const payload = JSON.parse(request.body);
    assert.equal(payload.nama, 'Anak Server');
    return { ok: true, status: 201 };
  };
  assert.equal(await h.app.sinkronkanAnakTertunda(), 0);
  assert.equal(child.syncStatus, 'synced');
});

test('monthly history opens latest record and correction returns to history', () => {
  const { app } = harness();
  const child = { id: '1', nik: '1', nama: 'Anak', inisial: 'AN', riwayat: { '2025': { 4: { tanggalUkur: '2025-05-10', bb: '8.1', pb: '74', lila: '13', lika: '44', idPengukuran: 'ukur_1_202505' } } } };
  app.bukaRiwayat(child, 'list');
  assert.equal(app.view, 'history');
  assert.equal(app.selectedYear, '2025');
  assert.equal(app.selectedMonthIndex, 4);
  assert.equal(app.recordBulanTerpilih.bb, '8.1');
  app.koreksiPengukuranAktif();
  assert.equal(app.view, 'form');
  assert.equal(app.isEditingRecord, true);
  assert.equal(app.previousViewBeforeForm, 'history');
});

test('recording defaults to queue and releases the service slot while KMS review stays visible', async () => {
  const h = harness();
  h.app.registryConfig.activeProvider = 'json_offline';
  const first = { id: '3273010101220001', nik: '3273010101220001', nama: 'Anak Satu', inisial: 'AS', umurBulan: 20, namaOrtu: 'Ibu Satu', rt: '01', tglLahir: '2025-01-01', riwayat: {} };
  const second = { id: '3273010101220002', nik: '3273010101220002', nama: 'Anak Dua', inisial: 'AD', umurBulan: 30, namaOrtu: 'Ibu Dua', rt: '02', tglLahir: '2024-01-01', riwayat: {} };
  h.app.daftarAnak = [first, second];
  h.app.queueEntries = [];
  h.app.view = 'queue';
  h.app.queueNotes[first.nik] = 'Batuk dua hari, mohon dicek.';
  assert.equal(h.app.tambahAntrean(first), true);
  assert.equal(h.app.tambahAntrean(second), true);
  assert.equal(h.app.antreanAktif.length, 2);
  assert.equal(h.app.formatNomorAntrean(h.app.antreanAktif[0].number), 'A01');
  assert.equal(h.app.antreanAktif[0].note, 'Batuk dua hari, mohon dicek.');
  assert.equal(h.app.tambahAntrean(first), false);
  assert.match(h.alerts.pop(), /Antrean Sudah Ada/);
  h.app.panggilAntrean(h.app.antreanAktif[0]);
  h.app.panggilAntrean(h.app.antreanAktif[1]);
  assert.equal(h.app.antreanAktif[0].status, 'called');
  assert.equal(h.app.antreanTerpanggil.length, 2);
  h.app.goToList('record');
  assert.equal(h.app.recordListTab, 'queue');
  assert.equal(h.app.filteredAnak.length, 2);
  const waitingChild = h.app.filteredAnak.find(child => child._queueItem.status === 'waiting');
  assert.equal(waitingChild, undefined);
  const calledChild = h.app.filteredAnak.find(child => child._queueItem.childIdentity === second.nik);
  h.app.pilihAnak(calledChild);
  assert.equal(h.app.view, 'form');
  assert.equal(h.app.activeQueueForForm.childIdentity, second.nik);
  h.context.window.AndroidBridge = {
    simpanDataPengukuran: () => JSON.stringify({ ok: true, action: 'inserted', revision: 1 }),
    simpanDaftarAnak: () => JSON.stringify({ ok: true }),
    simpanAntrean: () => JSON.stringify({ ok: true }),
    tampilkanPesan: () => {},
  };
  h.app.formVals = { bb: '11,2', pb: '88,5', lila: '15', lika: '48' };
  await h.app.simpanData();
  assert.equal(h.app.view, 'list');
  assert.equal(h.app.antreanSelesai.length, 0);
  assert.equal(h.app.antreanAktif.find(item => item.childIdentity === second.nik), undefined);
  assert.equal(h.app.antreanReviewKms.find(item => item.childIdentity === second.nik).status, 'kms_review');
  assert.equal(h.app.slotPanggilan.length, 2);
});

test('published card waits for a note decision before creating an active queue number', () => {
  const h = harness();
  h.context.window.AndroidBridge = {
    simpanAntrean: () => JSON.stringify({ ok: true }),
  };
  const child = {
    id: '3273010101220001', sourceId: 110, nik: '3273010101220001', nama: 'Anak Kartu',
    namaOrtu: 'Ibu Kartu', nikOrtu: '3273010101220099', tglLahir: '2024-01-01', jk: 'P',
    rt: '01', anakKe: 1, bbLahir: 3.1, pbLahir: 49, bukuKia: true, imd: true,
    imunisasiLengkap: true, riwayat: {},
  };
  h.app.daftarAnak = [child];
  h.app.queueEntries = [];
  assert.equal(h.app.prosesHasilScanKartu('SIMPATIK:SASARAN:1:110:3273010101220001'), true);
  assert.equal(h.app.queueNoteDialog, true);
  assert.equal(h.app.scannedAnak, child);
  assert.equal(h.app.antreanAktif.length, 0);
  h.app.queueNoteDraft = 'Bawa Buku KIA';
  assert.equal(h.app.masukkanScanKeAntrean(true), true);
  assert.equal(h.app.scannedAnak, null);
  assert.equal(h.app.antreanAktif.length, 1);
  assert.equal(h.app.antreanAktif[0].childIdentity, child.nik);
  assert.equal(h.app.antreanAktif[0].note, 'Bawa Buku KIA');
  assert.equal(h.app.formatNomorAntrean(h.app.antreanAktif[0].number), 'A01');
  assert.equal(h.app.queueTab, 'list');
  assert.equal(h.app.notice.type, 'success');
  assert.equal(h.app.prosesHasilScanKartu('SIMPATIK:SASARAN:1:110:3273010101220001'), false);
  assert.match(h.alerts.pop(), /Antrean Sudah Ada/);
});

test('printed SPT fallback code finds the same child as the QR payload', () => {
  const h = harness();
  const child = { id: '3273010101220001', sourceId: 23, kodeKartu: 'SPT-00000023', nik: '3273010101220001', nama: 'Anak Kartu', riwayat: {} };
  h.app.daftarAnak = [child];
  assert.equal(h.app.prosesHasilScanKartu('SPT-00000023'), true);
  assert.equal(h.app.queueNoteDialog, true);
  assert.equal(h.app.masukkanScanKeAntrean(false), true);
  assert.equal(h.app.antreanAktif.length, 1);
  assert.equal(h.app.antreanAktif[0].childIdentity, child.nik);
});

test('skipping a scan note discards the draft and repeated confirmation cannot duplicate the entry', () => {
  const h = harness();
  const child = { id: 'scan-skip', sourceId: 923, nama: 'Anak Uji Lewati', riwayat: {} };
  h.app.daftarAnak = [child];
  h.app.queueEntries = [];
  assert.equal(h.app.prosesHasilScanKartu('SIMPATIK:SASARAN:1:923'), true);
  assert.equal(h.app.antreanAktif.length, 0);
  h.app.queueNoteDraft = 'Draf tidak boleh tersimpan';
  assert.equal(h.app.masukkanScanKeAntrean(false), true);
  assert.equal(h.app.antreanAktif.length, 1);
  assert.equal(h.app.antreanAktif[0].note, '');
  assert.equal(h.app.masukkanScanKeAntrean(false), false);
  assert.equal(h.app.antreanAktif.length, 1);
});

test('Android back cancels a scanned card note without registering the child', () => {
  const h = harness();
  const child = { id: 'scan-cancel', sourceId: 924, nama: 'Anak Uji Batal', riwayat: {} };
  h.app.daftarAnak = [child];
  h.app.queueEntries = [];
  h.app.view = 'queue';
  h.app.prosesHasilScanKartu('SIMPATIK:SASARAN:1:924');
  h.app.queueNoteDraft = 'Batal';
  assert.equal(h.app.handleAndroidBack(), 'handled');
  assert.equal(h.app.queueNoteDialog, false);
  assert.equal(h.app.scannedAnak, null);
  assert.equal(h.app.queueNoteDraft, '');
  assert.equal(h.app.antreanAktif.length, 0);
  assert.equal(h.app.view, 'queue');
});

test('published targets are immediately visible and search narrows the roster', () => {
  const h = harness();
  const child = { id: '1', nik: '3273010101220001', nama: 'Aisyah Putri', inisial: 'AP', umurBulan: 20, namaOrtu: 'Ibu Aisyah', rt: '01', statusSasaran: 'menunggu', riwayat: {} };
  const completed = { id: '2', nik: '3273010101220002', nama: 'Budi Selesai', statusSasaran: 'selesai', riwayat: {} };
  h.app.daftarAnak = [child, completed];
  h.app.queueEntries = [];
  h.app.queueTab = 'add';
  h.app.queueSearch = '';
  assert.equal(h.app.anakUntukAntrean.length, 1);
  assert.equal(h.app.anakUntukAntrean[0].nama, 'Aisyah Putri');
  h.app.queueSearch = 'a';
  assert.equal(h.app.anakUntukAntrean.length, 1);
  h.app.queueSearch = 'tidak ada';
  assert.equal(h.app.anakUntukAntrean.length, 0);
  assert.equal(h.app.tambahAntrean(child), true);
  assert.equal(h.app.queueTab, 'list');
  assert.equal(h.app.queueSearch, '');
});

test('opening queue shows active check-ins or falls back to all pending targets', () => {
  const h = harness();
  h.app.daftarAnak = [{ id: '1', nama: 'Sasaran Live', statusSasaran: 'menunggu' }];
  h.app.queueEntries = [];
  h.app.bukaAntrean();
  assert.equal(h.app.queueTab, 'add');
  assert.equal(h.app.anakBelumBulanIni, 1);
  assert.equal(h.app.anakUntukAntrean.length, 1);
  h.app.queueEntries = [{ id: 'q1', date: h.app.tanggalHariIniISO, childIdentity: '1', status: 'waiting', sortOrder: 1, number: 1 }];
  h.app.bukaAntrean();
  assert.equal(h.app.queueTab, 'list');
});

test('calling and recalling a child repeats the audible queue announcement', () => {
  const h = harness();
  const announcements = [];
  h.context.window.AndroidBridge = {
    simpanAntrean: () => JSON.stringify({ ok: true }),
    ucapkanPanggilan: (number, name) => { announcements.push({ number, name }); return true; },
  };
  h.app.queueEntries = [{
    id: 'q1', date: h.app.tanggalHariIniISO, number: 7, sortOrder: 1,
    childIdentity: '327301', name: 'Adzkiya Rumaysita', status: 'waiting',
  }];
  const item = h.app.antreanAktif[0];
  h.app.panggilAntrean(item);
  assert.equal(item.status, 'called');
  h.app.panggilAntrean(item);
  assert.equal(item.status, 'called');
  assert.deepEqual(announcements, [
    { number: 'A07', name: 'Adzkiya Rumaysita' },
    { number: 'A07', name: 'Adzkiya Rumaysita' },
  ]);
});

test('KMS review stays visible without occupying either of the two service slots', () => {
  const h = harness();
  const called = { id: 'q1', date: h.app.tanggalHariIniISO, number: 1, status: 'called', name: 'Anak Pertama' };
  const measuring = { id: 'q2', date: h.app.tanggalHariIniISO, number: 2, status: 'serving', name: 'Anak Kedua' };
  const waiting = { id: 'q3', date: h.app.tanggalHariIniISO, number: 3, status: 'waiting', name: 'Anak Ketiga' };
  h.app.queueEntries = [called, measuring, waiting];
  assert.equal(h.app.antreanTerpanggil.length, 2);
  assert.equal(h.app.slotPanggilan.length, 2);
  assert.equal(h.app.selesaikanAntrean(measuring), true);
  assert.equal(measuring.status, 'kms_review');
  assert.equal(h.app.antreanReviewKms.length, 1);
  assert.equal(h.app.antreanAktif.includes(measuring), false);
  assert.equal(h.app.antreanTerpanggil.length, 1);
  assert.equal(h.app.antreanMenunggu[0], waiting);
});

test('queue sync uses the shared Portal GET, POST, and PATCH contract', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'session-token' }));
  h.app.registryConfig.activeProvider = 'portal_api';
  h.app.registryConfig.portalApi.url = portalApiBawaan;
  h.app.periodeSasaran = { periodeId: 7 };
  const date = h.app.tanggalHariIniISO;
  const child = { id: 'child-1', idSasaran: 44, nik: '3273010101220001', nama: 'Anak Portal', namaOrtu: 'Orang Tua', rt: '03' };
  h.app.daftarAnak = [child];
  h.app.queueEntries = [{
    id: 'local-1', serverId: null, sasaranId: 44, date, childIdentity: child.nik,
    name: child.nama, parent: child.namaOrtu, rt: child.rt, number: 1, sortOrder: 1,
    status: 'called', note: 'Perlu cek ulang', updatedAt: new Date().toISOString(), _queueDirty: true,
  }];
  const requests = [];
  const canonical = status => ({
    id: `queue-${date}-44`, periodeId: 7, sasaranId: 44, tanggal: date,
    nomor: 1, urutan: 1, childIdentity: child.nik, nama: child.nama,
    namaOrtu: child.namaOrtu, rt: child.rt, status, catatan: 'Perlu cek ulang',
    checkedInAt: new Date().toISOString(), updatedAt: new Date().toISOString(),
  });
  h.context.fetch = async (url, request = {}) => {
    requests.push({ url, request });
    if (!request.method) return { ok: true, json: async () => ({ items: [] }) };
    if (request.method === 'POST') return { ok: true, json: async () => ({ item: canonical('waiting') }) };
    if (request.method === 'PATCH') return { ok: true, json: async () => ({ item: canonical('called') }) };
    throw new Error('Unexpected Portal method: ' + request.method);
  };

  assert.equal(await h.app.sinkronkanAntreanPortal(), true);
  assert.deepEqual(requests.map(({ request }) => request.method || 'GET'), ['GET', 'POST', 'PATCH']);
  assert.match(requests[0].url, /\/antrean\?periodeId=7&tanggal=/);
  assert.equal(requests[1].url, portalApiBawaan + '/antrean');
  assert.deepEqual(JSON.parse(requests[1].request.body), { periodeId: 7, sasaranId: 44, tanggal: date, catatan: 'Perlu cek ulang' });
  assert.equal(requests[2].url, portalApiBawaan + '/antrean/queue-' + date + '-44');
  assert.deepEqual(JSON.parse(requests[2].request.body), { status: 'called', catatan: 'Perlu cek ulang', urutan: 1 });
  assert.equal(h.app.queueEntries[0].serverId, 'queue-' + date + '-44');
  assert.equal(h.app.queueEntries[0].status, 'called');
  assert.equal(!!h.app.queueEntries[0]._queueDirty, false);
  assert.equal(h.app.queueSyncError, '');
});

test('queue measurement date stays inside the active target period', () => {
  const h = harness();
  h.app.periodeSasaran = { periode: '2026-09', label: 'September 2026' };
  assert.equal(h.app.tanggalUkurSasaranAktif(), '2026-09-30');
  const child = { id: '3277304902244330', nik: '3277304902244330', nama: 'ADZKIYA RUMAYSITA', umurBulan: 32, riwayat: {} };
  h.app.daftarAnak = [child];
  h.app.listMode = 'record';
  h.app.queueEntries = [{
    id: 'q-adzkiya', date: h.app.tanggalHariIniISO, number: 1, sortOrder: 1,
    childIdentity: child.nik, name: child.nama, status: 'called',
  }];
  h.app.goToList('record');
  const queuedChild = h.app.filteredAnak[0];
  h.app.pilihAnak(queuedChild);
  assert.equal(h.app.tanggalUkur, '2026-09-30');
});

test('closed-session absentees appear in the manual follow-up list for the target month', () => {
  const h = harness();
  h.app.periodeSasaran = { periodeId: 7, periode: '2026-09', label: 'September 2026', ringkasan: { menunggu: 0 } };
  const child = { id: '327301', nik: '3273010101220001', nama: 'Anak Susulan', umurBulan: 20, statusSasaran: 'tidak_hadir', riwayat: {} };
  h.app.daftarAnak = [child, { id: '2', nama: 'Sudah Selesai', statusSasaran: 'selesai', riwayat: {} }];
  h.app.recordListTab = 'followup';
  h.app.listMode = 'record';
  assert.equal(h.app.sasaranSusulan.length, 1);
  assert.equal(h.app.filteredAnak[0].nama, 'Anak Susulan');
  h.app.pilihAnak(h.app.filteredAnak[0]);
  assert.equal(h.app.tanggalUkur, '2026-09-30');
  h.app.recordListTab = 'all';
  h.app.pilihAnak(child);
  assert.equal(h.app.tanggalUkur, '2026-09-30');
});

test('late follow-up is marked only after the server confirms its database write', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'session-token' }));
  h.app.registryConfig.activeProvider = 'portal_api';
  h.app.registryConfig.portalApi.url = portalApiBawaan;
  const child = { id: '327301', nik: '3273010101220001', nama: 'Anak Susulan', statusSasaran: 'tidak_hadir', riwayat: { '2026': { 8: { idPengukuran: 'susulan-1', syncStatus: 'pending' } } } };
  h.app.daftarAnak = [child];
  h.context.fetch = async () => ({ ok: true, json: async () => ({ pengukuran: { id: 54, susulan: true } }) });
  const result = await h.app.sinkronkanSatuPengukuran({ id_pengukuran: 'susulan-1', nik: child.nik, tanggal_ukur: '2026-09-30' });
  assert.equal(result.susulan, true);
  assert.equal(child.statusSasaran, 'tidak_hadir');
  assert.equal(child.pendataanSusulan, true);
  assert.equal(child.riwayat['2026'][8].syncStatus, 'synced');
  assert.equal(child.riwayat['2026'][8].pendataanSusulan, true);
  assert.equal(child.riwayat['2026'][8].pendataanSusulanPending, false);
});

test('measurement comparison asks kader to repeat an implausible decrease', () => {
  const { app } = harness();
  app.activeAnak = {
    id: '3273010101220001', nik: '3273010101220001', tglLahir: '2022-01-01',
    riwayat: { '2026': { 7: { tanggalUkur: '2026-08-10', bb: '15', pb: '95', lila: '15', lika: '48' } } },
  };
  app.tanggalUkur = '2026-09-10';
  app.formVals = { bb: '13', pb: '93', lila: '15', lika: '48' };
  assert.match(app.peringatanPengukuran.join(' '), /BB turun 2\.0 kg/);
  assert.match(app.peringatanPengukuran.join(' '), /TB\/PB berkurang 2\.0 cm/);
});

test('LILA and LIKA are prefilled from the previous month without becoming new measurements', () => {
  const { app } = harness();
  app.periodeSasaran = { periode: '2026-09', ringkasan: { menunggu: 0 } };
  const child = {
    id: '327301', nik: '3273010101220001', nama: 'Anak', umurBulan: 20,
    riwayat: { '2026': { 7: { tanggalUkur: '2026-08-10', lila: '15.2', lika: '48' } } },
  };
  app.bukaFormPengukuran(child, '2026-09-10');
  assert.equal(app.formVals.lila, '15,2');
  assert.equal(app.formVals.lika, '48');
  assert.equal(app.formTambahanDiwariskan.lila, true);
  assert.equal(app.formTambahanDiwariskan.lika, true);
});

test('closing a session marks every remaining target absent without deleting children', async () => {
  const h = harness();
  h.storage.set('SIMPATIK_SESSION', JSON.stringify({ token: 'session-token' }));
  h.app.periodeSasaran = {
    periodeId: 7,
    label: 'September 2026',
    ringkasan: { total: 3, menunggu: 2, selesai: 1, tidakHadir: 0, pindah: 0 },
  };
  h.app.daftarAnak = [
    { id: '1', nama: 'Satu', statusSasaran: 'menunggu' },
    { id: '2', nama: 'Dua', statusSasaran: 'menunggu' },
    { id: '3', nama: 'Tiga', statusSasaran: 'selesai' },
  ];
  h.app.queueEntries = [{ id: 'q1', date: h.app.tanggalHariIniISO, childIdentity: '1', status: 'waiting' }];
  h.app.persistAntrean = () => {};
  h.context.fetch = async (url, request) => {
    assert.equal(url, portalApiBawaan + '/sasaran/tutup-sesi');
    assert.equal(request.headers.Authorization, 'Bearer session-token');
    assert.deepEqual(JSON.parse(request.body), { periodeId: 7, konfirmasi: true });
    return { ok: true, json: async () => ({ hasil: { ditandaiTidakHadir: 2, sesiDitutupPada: '2026-09-10T05:00:00Z' } }) };
  };
  await h.app.konfirmasiTutupSesi();
  assert.deepEqual(h.app.daftarAnak.map(item => item.statusSasaran), ['tidak_hadir', 'tidak_hadir', 'selesai']);
  assert.equal(h.app.periodeSasaran.ringkasan.menunggu, 0);
  assert.equal(h.app.periodeSasaran.ringkasan.tidakHadir, 2);
  assert.equal(h.app.queueEntries[0].status, 'cancelled');
  assert.match(h.alerts.pop(), /Sesi Berhasil Ditutup/);
});

test('tablet UI exposes operational modules without portal analytics modules', () => {
  assert.match(html, /Pencatatan Lapangan/);
  assert.match(html, /Pencatatan Langsung/);
  assert.match(html, /Riwayat Bulanan/);
  assert.match(html, /Pendaftaran Balita Baru/);
  assert.match(html, /Antrean Hari Ini/);
  assert.match(html, /Sasaran Belum Dilayani/);
  assert.match(html, /Pindai Kartu dengan Kamera/);
  assert.match(html, /Kosongkan pencarian untuk melihat semuanya/);
  assert.match(html, /Daftar layanan/);
  assert.match(html, /Dari Antrean/);
  assert.match(html, /Semua Balita/);
  assert.match(html, /Mode cepat aktif/);
  assert.match(html, /setelah pengukuran, anak menunggu pemeriksaan KMS di luar slot/i);
  assert.match(html, /home-summary/);
  assert.match(html, /home-progress-grid/);
  assert.match(html, /history-header/);
  assert.match(html, /Susulan di Luar Sesi/);
  assert.match(html, /Panggil Lagi/);
  assert.doesNotMatch(html, /Mulai Pengukuran/);
  assert.match(html, /Dibuat oleh POLMAN Bandung/);
  assert.match(html, /© 2026 POLMAN Bandung/);
  assert.match(html, /Progres sasaran/);
  assert.match(html, /daftarAnak\.length \+ ' sasaran tersedia'/);
  assert.match(html, /Konfirmasi beres sesi/);
  assert.match(html, /sasaran\/tutup-sesi/);
  assert.doesNotMatch(html, /view === 'nutrition_detail'/);
  assert.doesNotMatch(html, /view === 'settings'/);
  assert.doesNotMatch(html, /kartuHasil\(\)/);
  assert.doesNotMatch(html, /hasilSimulasiZScore\(\)/);
});

test('Nearby queue snapshots accept only the same workspace and merge by stable ID', () => {
  const h = harness();
  const today = h.app.tanggalHariIniISO;
  h.app.nearbyWorkspaceId = 'posyandu:18';
  h.app.queueEntries = [{ id: 'queue-a', date: today, status: 'waiting', updatedAt: '2026-10-19T01:00:00Z' }];
  h.app.terapkanSnapshotCluster({ workspaceId: 'posyandu:19', queue: [{ id: 'wrong-rw', date: today }] });
  assert.equal(h.app.queueEntries.length, 1);
  h.app.terapkanSnapshotCluster({ workspaceId: 'posyandu:18', queue: [
    { id: 'queue-a', date: today, status: 'done', updatedAt: '2026-10-19T02:00:00Z' },
    { id: 'queue-b', date: today, status: 'waiting', updatedAt: '2026-10-19T02:01:00Z' },
    { id: 'old-day', date: '2026-10-18', status: 'waiting' },
  ] });
  assert.equal(h.app.queueEntries.length, 2);
  assert.equal(h.app.queueEntries.find(item => item.id === 'queue-a').status, 'done');
  assert.equal(h.app.queueEntries.some(item => item.id === 'old-day'), false);
});

test('application notices replace raw WebView alerts with semantic styled dialogs', () => {
  const { app } = harness();
  app.tampilkanNotifikasi('[Data Belum Valid]\n\nBerat badan wajib diisi sebelum menyimpan.');
  assert.equal(app.notice.visible, true);
  assert.equal(app.notice.type, 'error');
  assert.equal(app.notice.title, 'Data Belum Valid');
  assert.equal(app.notice.message, 'Berat badan wajib diisi sebelum menyimpan.');
  assert.equal(app.ikonNotifikasi(), 'ph-fill ph-x-circle');
  app.tutupNotifikasi();
  assert.equal(app.notice.visible, false);

  app.tampilkanNotifikasi('[Masuk Antrean]\n\nA01 · ADZKIYA RUMAYSITA masuk antrean.');
  assert.equal(app.notice.type, 'success');
  assert.equal(app.notice.title, 'Masuk Antrean');
  assert.match(html, /role="alertdialog"/);
  assert.match(html, />Mengerti<\/button>/);
});


test('VM deployment migrates the saved laptop API without deleting session or offline work', () => {
  const saved = {
    SIMPATIK_REGISTRY_CONFIG: JSON.stringify({portalApi:{url:'http://192.168.1.10:4321/api/v1'},connectionMode:'lokal'}),
    SIMPATIK_SESSION: JSON.stringify({token:'uji-session'}),
    SIMPATIK_DATA_ANAK: JSON.stringify([{id:'offline-1',nama:'Anak Uji',syncStatus:'pending'}]),
  };
  const h = harness(saved);
  assert.equal(h.app.registryConfig.portalApi.url, portalApiBawaan);
  assert.equal(h.app.registryConfig.connectionMode, 'online');
  assert.equal(h.app.registryConfig.portalDeployment, 'vm-2026-10-23');
  assert.equal(h.storage.get('SIMPATIK_SESSION'), saved.SIMPATIK_SESSION);
  assert.equal(h.app.daftarAnak[0].syncStatus, 'pending');
  const ulang = harness({SIMPATIK_REGISTRY_CONFIG:JSON.stringify({...h.app.registryConfig,portalApi:{url:'https://operator.example/api/v1'}})});
  assert.equal(ulang.app.registryConfig.portalApi.url, 'https://operator.example/api/v1');
});

test('native discovery and WebView use the same VM hostname', () => {
  const native = fs.readFileSync(path.join(__dirname,'../app/src/main/java/com/example/simpatikposyandu/MainActivity.kt'),'utf8');
  const host = native.match(/val cloudflareHost = "([^"]+)"/)[1];
  assert.equal(host, new URL(portalApiBawaan).hostname);
});

 test('healthy connection still retries pending uploads and refreshes the roster', async () => {
  const h=harness({'SIMPATIK_SESSION':'{"token":"test"}'});let runs=0;
  h.app.registryConfig.portalApi.status='API & database siap';
  h.app.ujiKoneksiPortal=async()=>true;h.app.kirimHeartbeat=()=>{};
  h.app.sinkronkanSetelahTerhubung=async()=>{runs++;};
  await h.app.pulihkanKoneksiDanSinkronkan();await new Promise(r=>setTimeout(r,0));
  await h.app.pulihkanKoneksiDanSinkronkan();await new Promise(r=>setTimeout(r,0));
  assert.equal(runs,2);
 });
 test('roster refresh preserves pending registration and unsent measurements', async () => {
  const h=harness({'SIMPATIK_SESSION':'{"token":"test"}'});
  h.app.daftarAnak=[{id:'1',nik:'1',riwayat:{'2026':{9:{idPengukuran:'u1',syncStatus:'pending',bb:'12'}}}}, {id:'2',nik:'2',nama:'Baru',syncStatus:'pending',riwayat:{}}];
  h.context.fetch=async()=>({ok:true,json:async()=>({anak:[{nik:'1',nama_anak:'Lama',tgl_lahir:'2024-01-01',jk:'L'}],pengukuran:[],sasaran:null})});
  assert.equal(await h.app.muatDataDariSupabase(),true);
  assert.equal(h.app.daftarAnak.length,2);assert.equal(h.app.daftarAnak[0].riwayat['2026'][9].bb,'12');
 });
 test('late upload acknowledgement cannot clear a newer local correction', async () => {
  const old={id_pengukuran:'u1',nik:'1',tanggal_ukur:'2026-10-23',bb_kg:'12'};
  const newer={...old,bb_kg:'13'};
  const h=harness({'SIMPATIK_SESSION':'{"token":"test"}','SIMPATIK_PENDING_MEASUREMENTS':JSON.stringify([old])});
  h.context.fetch=async()=>{h.storage.set('SIMPATIK_PENDING_MEASUREMENTS',JSON.stringify([newer]));return{ok:true,json:async()=>({pengukuran:{}})};};
  assert.equal(await h.app.sinkronkanSatuPengukuran(old),false);
  assert.equal(h.app.ambilAntreanPengukuran()[0].bb_kg,'13');
 });
 test('nearby relay uploads through the connected tablet and shares an exact receipt', async () => {
  const a=harness({'SIMPATIK_SESSION':'{"token":"a"}'}),b=harness({'SIMPATIK_SESSION':'{"token":"b"}'});
  const payload={id_pengukuran:'relay-1',nik:'1234567890123456',tanggal_ukur:'2026-10-23',bb_kg:'12',panjang_tinggi_cm:'85'};
  for(const h of [a,b]){h.app.nearbyWorkspaceId='rw18';h.app.daftarAnak=[];h.app.sinkronkanAntreanPortal=async()=>true;}
  let pendingA=[payload],pendingB=[];
  a.context.window.AndroidBridge={ambilPengukuranTertunda:()=>JSON.stringify(pendingA),tandaiPengukuranTersinkron:()=>{pendingA=[];}};
  b.context.window.AndroidBridge={ambilPengukuranTertunda:()=>JSON.stringify(pendingB),imporSnapshotPengukuranNearby:raw=>{pendingB=JSON.parse(raw).map(x=>x.payload);return pendingB.length;},tandaiPengukuranTersinkron:()=>{pendingB=[];},kirimSnapshotNearby:raw=>{a.app.terapkanSnapshotCluster(raw);return 1;},ambilSnapshotPengukuranTertunda:()=>JSON.stringify(pendingB.map(payload=>({payload,updatedAt:1})))};
  b.app.sinkronkanSetelahTerhubung=async()=>{};
  b.app.terapkanSnapshotCluster({workspaceId:'rw18',queue:[],pendingMeasurements:[{payload,updatedAt:1}]});
  let posts=0;b.context.fetch=async()=>{posts++;return{ok:true,json:async()=>({pengukuran:{}})};};
  await b.app.sinkronkanDataTertunda();assert.equal(posts,1);assert.equal(pendingB.length,0);assert.equal(pendingA.length,0);
 });

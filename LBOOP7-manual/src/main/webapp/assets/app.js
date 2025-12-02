const state = {
    auth: null,
    factory: 'ARRAY',
    functions: [],
    tooltip: null,
    chartView: null,
    gallerySelection: null,
};

let chartDropdownListenerBound = false;
const fancyDropdowns = {};
let chartSearch = '';

function bind(id, event, handler) {
    const el = document.getElementById(id);
    if (el) {
        el.addEventListener(event, handler);
    }
    return el;
}

function ready(fn) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', fn, { once: true });
    } else {
        fn();
    }
}

// Универсальный генератор идентификаторов (не требует crypto.randomUUID в старых браузерах)
function uid() {
    const hasCrypto = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function';
    return (hasCrypto ? crypto.randomUUID() : `fn-${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`);
}

// Поддержка приветственного оверлея (если он отсутствует на странице — это no-op)
const gate = document.getElementById('welcomeGate');
function openGate() { if (gate) gate.hidden = false; }
function closeGate() { if (gate) gate.hidden = true; }

const api = {
    async request(path, options = {}) {
        const headers = options.headers || {};
        if (state.auth?.credentials) {
            headers['Authorization'] = `Basic ${state.auth.credentials}`;
        }
        const response = await fetch(path, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...headers,
            },
        });

        if (response.status === 401) {
            showModal('Требуется авторизация', 'Войдите, чтобы работать с сервером. Мы перенаправим вас на страницу входа.');
            setTimeout(() => window.location.href = '/auth.html', 600);
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || response.statusText);
        }
        return response.status === 204 ? null : response.json();
    },
    async fetchCurrentUser(username) {
        return this.request(`/users?username=${encodeURIComponent(username)}`);
    },
    async fetchFunctions(ownerId) {
        return this.request(`/functions?ownerId=${ownerId}`);
    },
    async fetchPoints(functionId) {
        return this.request(`/points?functionId=${functionId}`);
    },
    async createUser(payload) {
        return this.request('/users', { method: 'POST', body: JSON.stringify(payload) });
    },
    async createFunction(payload) {
        return this.request('/functions', { method: 'POST', body: JSON.stringify(payload) });
    },
    async createPoints(payload) {
        return this.request('/points', { method: 'POST', body: JSON.stringify(payload) });
    },
    async operate(payload) {
        return this.request('/operations', { method: 'POST', body: JSON.stringify(payload) });
    },
};

function pointsFromResponse(response) {
    if (!response?.xValues || !response?.yValues)
        return [];
    return response.xValues.map((x, i) => ({ x, y: response.yValues[i] }));
}

const simpleFunctions = [
    { key: 'identity', label: 'Тождественная (IdentityFunction)', build: () => (x) => x },
    { key: 'square', label: 'Квадратичная (SqrFunction)', build: () => (x) => x * x },
    { key: 'sin', label: 'Синус (SinFunction)', build: () => (x) => Math.sin(x) },
    { key: 'zero', label: 'Нулевая (ZeroFunction)', build: () => () => 0 },
    { key: 'unit', label: 'Единичная (UnitFunction)', build: () => () => 1 },
    { key: 'identityShift', label: 'Сдвиг (Identity + 1)', build: () => (x) => x + 1 },
    {
        key: 'constant',
        label: 'Константная (ConstantFunction)',
        params: [{ key: 'value', label: 'Константа', type: 'number', default: 1 }],
        build: (params) => () => Number(params.value ?? 0),
    },
    {
        key: 'newton',
        label: 'Метод Ньютона (MethodNewtonFunction)',
        params: [
            { key: 'base', label: 'Базовая функция (identity/sin/sqr)', type: 'text', default: 'sin' },
        ],
        build: (params) => {
            const base = (params.base || 'sin').toLowerCase();
            const baseFn = base === 'sqr' ? (x) => x * x - 2 : base === 'identity' ? (x) => x : (x) => Math.sin(x);
            return (x0) => newtonSolve(baseFn, x0);
        },
    },
    {
        key: 'deboor',
        label: 'B-сплайн де Бура (DeBoorFunction)',
        params: [
            { key: 'knots', label: 'Узлы (через запятую)', type: 'text', default: '0,0,0,1,2,3,3,3' },
            { key: 'controls', label: 'Контрольные точки', type: 'text', default: '0,1,0,2,0' },
            { key: 'degree', label: 'Степень', type: 'number', default: 2 },
        ],
        build: (params) => {
            const knots = parseNumberList(params.knots);
            const ctrl = parseNumberList(params.controls);
            const degree = Number(params.degree ?? 2);
            if (knots.length !== ctrl.length + degree + 1 || degree < 1) {
                return () => NaN;
            }
            return (x) => deBoorEvaluate(x, knots, ctrl, degree);
        },
    },
    {
        key: 'compositeDoubleSin',
        label: 'Сложная: 2·sin(x) (CompositeFunction)',
        build: () => (x) => 2 * Math.sin(x),
    },
];

function renderSimpleGallery() {
    const gallery = document.getElementById('simpleGallery');
    if (!gallery) return;
    gallery.innerHTML = '';
    const sorted = simpleFunctions.slice().sort((a, b) => a.label.localeCompare(b.label));
    sorted.forEach(def => {
        const card = document.createElement('button');
        card.type = 'button';
        card.className = 'fn-card';
        card.dataset.key = def.key;
        const params = def.params?.length ? `${def.params.length} параметров` : 'Без параметров';
        card.innerHTML = `
            <div class="fn-card__title">${def.label}</div>
            <div class="fn-card__meta">${params}</div>
        `;
        card.addEventListener('click', () => {
            const select = document.getElementById('simpleFunctionSelect');
            select.value = def.key;
            state.gallerySelection = def.key;
            document.querySelectorAll('.fn-card').forEach(c => c.classList.toggle('active', c.dataset.key === def.key));
            renderSimpleParams();
        });
        gallery.appendChild(card);
    });
    const activeKey = document.getElementById('simpleFunctionSelect').value;
    document.querySelectorAll('.fn-card').forEach(c => c.classList.toggle('active', c.dataset.key === activeKey));
}

function bootstrapDemoFunctions() {
    if (state.functions.length) return;
    const sampleRange = { from: 0, to: 10, count: 8 };
    simpleFunctions.forEach(def => {
        const params = {};
        def.params?.forEach(p => params[p.key] = p.default ?? '');
        const mathFn = def.build ? def.build(params) : (x) => x;
        const step = (sampleRange.to - sampleRange.from) / (sampleRange.count - 1);
        const points = Array.from({ length: sampleRange.count }, (_, i) => {
            const x = sampleRange.from + i * step;
            return { x, y: mathFn(x) };
        });
        pushFunction(def.label, points, { persisted: false, insertable: true, removable: true });
    });
}

const modal = document.getElementById('modal');
const modalTitle = document.getElementById('modalTitle');
const modalMessage = document.getElementById('modalMessage');
const tooltipEl = document.createElement('div');
tooltipEl.className = 'canvas-tooltip';
document.body.appendChild(tooltipEl);
const chartOverlay = document.getElementById('chartOverlay');

const savedAuth = localStorage.getItem('lab7Auth');
const savedOffline = localStorage.getItem('lab7Offline');
const savedTheme = localStorage.getItem('lab7Theme');
if (savedAuth) {
    try {
        state.auth = JSON.parse(savedAuth);
        if (!state.auth.username && state.auth.user?.username) {
            state.auth.username = state.auth.user.username;
        }
    } catch (e) {
        localStorage.removeItem('lab7Auth');
    }
}
if (savedOffline) {
    state.offline = true;
}
if (savedTheme === 'light') {
    document.body.classList.add('light');
    const toggle = document.getElementById('darkModeToggle');
    if (toggle) toggle.checked = false;
}

function parseNumberList(value) {
    if (value === undefined || value === null) return [];
    return value.toString().split(/[,;\s]+/).map(v => parseFloat(v)).filter(v => !Number.isNaN(v));
}

function parseRange(value) {
    if (!value) return null;
    const parts = value.split(/[,;\s;]+/).filter(Boolean);
    if (parts.length < 2) return null;
    const min = parseFloat(parts[0]);
    const max = parseFloat(parts[1]);
    if (!Number.isFinite(min) || !Number.isFinite(max) || max <= min) return null;
    return { min, max };
}

function functionsStorageKey() {
    const username = state.auth?.username || 'guest';
    return `lab7Functions_${username}`;
}

function persistLocalFunctions() {
    try {
        localStorage.setItem(functionsStorageKey(), JSON.stringify(state.functions));
    } catch (e) {
        // Игнорируем ошибки квоты/доступа, так как это лишь локальный кэш
    }
}

function restoreLocalFunctions() {
    try {
        const raw = localStorage.getItem(functionsStorageKey());
        if (!raw) return false;
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) return false;
        state.functions = parsed.map(fn => ({
            ...fn,
            points: Array.isArray(fn.points) ? fn.points.map(p => ({ x: Number(p.x), y: Number(p.y) })) : [],
        }));
        syncFunctionSelects();
        drawChart();
        return state.functions.length > 0;
    } catch (e) {
        return false;
    }
}

async function flushPendingToServer() {
    const user = await ensureAuthUser();
    if (!user) return;
    const pending = state.functions.filter(fn => !fn.persisted && fn.points?.length);
    for (const fn of pending) {
        try {
            const created = await api.createFunction({ name: fn.name, type: 'TABULATED', ownerId: user.id });
            const functionId = created?.id ?? created?.functionId ?? created?.function?.id ?? fn.id;
            await api.createPoints({ xValues: fn.points.map(p => p.x), yValues: fn.points.map(p => p.y), functionId });
            fn.id = functionId;
            fn.persisted = true;
        }
        catch (error) {
            // Оставляем функцию локально без всплывающих ошибок, чтобы пользователь продолжал работу
        }
    }
    persistLocalFunctions();
}

function normalizePoints(points) {
    return points
        .map(p => ({ x: Number(p.x), y: Number(p.y) }))
        .sort((a, b) => a.x - b.x);
}

function hasSamePoints(left, right) {
    if (!left?.points || !right?.points) return false;
    if (left.points.length !== right.points.length) return false;
    const a = normalizePoints(left.points);
    const b = normalizePoints(right.points);
    for (let i = 0; i < a.length; i++) {
        if (a[i].x !== b[i].x || a[i].y !== b[i].y) return false;
    }
    return true;
}

function findDuplicate(points) {
    if (!points?.length) return null;
    const normalized = normalizePoints(points);
    return state.functions.find(fn => {
        if (fn.points.length !== normalized.length) return false;
        const target = normalizePoints(fn.points);
        for (let i = 0; i < target.length; i++) {
            if (target[i].x !== normalized[i].x || target[i].y !== normalized[i].y) return false;
        }
        return true;
    }) || null;
}

function newtonSolve(fn, start) {
    let x = Number.isFinite(start) ? start : 0;
    const EPS = 1e-10;
    const H = 1e-6;
    for (let i = 0; i < 80; i++) {
        const df = (fn(x + H) - fn(x - H)) / (2 * H);
        if (Math.abs(df) < EPS) break;
        const next = x - fn(x) / df;
        if (Math.abs(next - x) < EPS) {
            x = next;
            break;
        }
        x = next;
    }
    return x;
}

function deBoorEvaluate(x, knots, controlPoints, degree) {
    if (x < knots[0] || x > knots[knots.length - 1]) return 0;
    const findSpan = () => {
        const n = controlPoints.length - 1;
        if (x >= knots[n + 1]) return n;
        if (x <= knots[degree]) return degree;
        let low = degree, high = n + 1, mid = Math.floor((low + high) / 2);
        while (x < knots[mid] || x >= knots[mid + 1]) {
            if (x < knots[mid]) high = mid; else low = mid;
            mid = Math.floor((low + high) / 2);
        }
        return mid;
    };

    const span = findSpan();
    if (span < degree || span >= knots.length - degree - 1) return 0;
    const d = [];
    for (let i = 0; i <= degree; i++) d[i] = controlPoints[span - degree + i];
    for (let r = 1; r <= degree; r++) {
        for (let j = degree; j >= r; j--) {
            const i = span - degree + j;
            const left = knots[i];
            const right = knots[i + degree + 1 - r];
            if (right === left) continue;
            const alpha = (x - left) / (right - left);
            d[j] = (1 - alpha) * d[j - 1] + alpha * d[j];
        }
    }
    return d[degree];
}

function showModal(title, message) {
    modalTitle.textContent = title;
    modalMessage.textContent = message;
    modal.hidden = false;
}

function hideModal() { modal.hidden = true; }
bind('modalClose', 'click', hideModal);
modal?.addEventListener('click', (event) => {
    if (event.target === modal) {
        hideModal();
    }
});
window.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !modal.hidden) {
        hideModal();
    }
});

function updateAuthBadge() {
    const badge = document.getElementById('authStatus');
    if (state.auth?.username) badge.textContent = `Вошли как ${state.auth.username}`;
    else if (state.offline) badge.textContent = 'Оффлайн режим';
    else badge.textContent = 'Не авторизован';
}

function normalizeUser(response) {
    if (!response) return null;
    if (Array.isArray(response)) return response[0] || null;
    if (response.user) return response.user;
    return response;
}

async function ensureAuthUser() {
    if (!state.auth?.username || !state.auth?.credentials) return null;
    if (state.auth.user?.id) return state.auth.user;
    try {
        const fetched = await api.fetchCurrentUser(state.auth.username);
        const user = normalizeUser(fetched);
        if (!user?.id) {
            showModal('Авторизация', 'Не удалось получить профиль пользователя.');
            return null;
        }
        state.auth.user = user;
        localStorage.setItem('lab7Auth', JSON.stringify(state.auth));
        updateAuthBadge();
        return user;
    }
    catch (error) {
        showModal('Авторизация', error.message);
        return null;
    }
}

bind('authPageBtn', 'click', () => window.location.href = '/auth.html');

async function handleLogin() {
    const username = document.getElementById('loginUsername')?.value.trim();
    const password = document.getElementById('loginPassword')?.value;
    if (!username || !password) {
        showModal('Ошибка', 'Введите имя и пароль.');
        return false;
    }

    const credentials = btoa(`${username}:${password}`);
    state.auth = { username, credentials, password };

    try {
        const fetched = await api.fetchCurrentUser(username);
        state.auth.user = normalizeUser(fetched);
        if (!state.auth.user?.id) throw new Error('Пользователь не найден');
        updateAuthBadge();
        restoreLocalFunctions();
        await flushPendingToServer();
        await syncFromServer();
        showModal('Успех', 'Аутентификация прошла успешно. Данные синхронизированы.');
        closeGate();
        return true;
    }

    catch (error) {
        state.auth = null;
        updateAuthBadge();
        showModal('Не удалось авторизоваться', error.message);
        return false;
    }
}

async function handleRegister() {
    const username = document.getElementById('loginUsername')?.value.trim();
    const password = document.getElementById('loginPassword')?.value;

    if (!username || !password) {
        showModal('Ошибка', 'Заполните почту и пароль для регистрации.');
        return false;
    }

    try {
        const created = await api.createUser({
            username: username,
            passwordHash: password,
            email: '',
            confirmPassword: password,
        });

        state.auth = { username, credentials: btoa(`${username}:${password}`), user: normalizeUser(created) || created };
        updateAuthBadge();
        restoreLocalFunctions();
        await flushPendingToServer();
        await syncFromServer();
        showModal('Регистрация успешна', 'Учётная запись создана, вы вошли в систему.');
        closeGate();
        return true;
    }

    catch (error) {
        showModal('Не удалось зарегистрировать', error.message);
        return false;
    }
}

function setFactory(factory) {
    state.factory = factory;
    document.querySelectorAll('.segmented button').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.factory === factory);
    });
}

function setDarkMode(enabled) {
    document.body.classList.toggle('light', !enabled);
    localStorage.setItem('lab7Theme', enabled ? 'dark' : 'light');
}

const darkToggle = document.getElementById('darkModeToggle');
if (darkToggle) {
    darkToggle.addEventListener('change', e => setDarkMode(e.target.checked));
}
document.querySelectorAll('.segmented button').forEach(btn => {
    btn.addEventListener('click', () => setFactory(btn.dataset.factory));
});

async function syncFromServer() {
    const user = await ensureAuthUser();
    if (!user) return;
    await flushPendingToServer();
    const pending = state.functions.filter(fn => !fn.persisted);
    try {
        const functions = await api.fetchFunctions(user.id);
        const hydrated = [];
        for (const fn of functions) {
            const points = await api.fetchPoints(fn.id);
            const zipped = (points[0] ? points[0].xValues.map((x, idx) => ({ x, y: points[0].yValues[idx] })) : []);
            hydrated.push({
                id: fn.id,
                name: fn.name,
                factory: state.factory,
                points: zipped,
                insertable: true,
                removable: true,
                persisted: true,
            });
        }
        state.functions = hydrated;
        pending.forEach(pendingFn => {
            const exists = hydrated.some(h => h.name === pendingFn.name || hasSamePoints(h, pendingFn));
            if (!exists) state.functions.push(pendingFn);
        });
        syncFunctionSelects();
        persistLocalFunctions();
    }

    catch (error) {
        showModal('Синхронизация', `Не удалось загрузить данные: ${error.message}`);
    }
}

function addPointRow(x = '', y = '') {
    const row = document.createElement('div');
    row.className = 'table-row';
    row.innerHTML = `
        <input type="number" step="any" value="${x}">
        <input type="number" step="any" value="${y}">
        <button class="ghost">✕</button>
    `;
    row.querySelector('button').addEventListener('click', () => row.remove());
    document.getElementById('pointsTable').appendChild(row);
}

function clearPoints() {
    document.querySelectorAll('#pointsTable .table-row').forEach(row => row.remove());
}

bind('addPointRow', 'click', () => addPointRow());
bind('clearPoints', 'click', clearPoints);
bind('shufflePoints', 'click', () => {
    clearPoints();
    Array.from({ length: 4 }).forEach(() => {
        addPointRow((Math.random() * 10).toFixed(2), (Math.random() * 12 - 4).toFixed(2));
    });
    showModal('Готово', 'Сгенерированы случайные точки. Отредактируйте при необходимости.');
});

function gatherPoints() {
    const rows = Array.from(document.querySelectorAll('#pointsTable .table-row'));
    const points = rows.map(r => ({
        x: parseFloat(r.children[0].value),
        y: parseFloat(r.children[1].value),
    })).filter(p => !Number.isNaN(p.x) && !Number.isNaN(p.y));
    if (points.length < 2) {
        showModal('Ошибка', 'Нужно минимум две точки.');
        return null;
    }
    if (points.some(p => Math.abs(p.x) > 1e6 || Math.abs(p.y) > 1e6)) {
        showModal('Слишком большое значение', 'Ограничьте модуль координат значением 1e6.');
        return null;
    }
    points.sort((a, b) => a.x - b.x);
    return points;
}

function syncFunctionSelects() {
    ['opFirst', 'opSecond', 'diffSource', 'integralSource', 'chartSource', 'saveSource'].forEach(id => {
        const select = document.getElementById(id);
        select.innerHTML = '';
        state.functions.forEach(fn => {
            const option = document.createElement('option');
            option.value = fn.id;
            option.textContent = `${fn.name} (${fn.points.length} тчк)`;
            select.appendChild(option);
        });
    });
    syncComposite();
    renderAllDropdowns();
    renderChartPicker();
}

function closeFancyMenus(except) {
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
        if (!except || !menu.closest('.dropdown').isSameNode(except)) {
            menu.hidden = true;
            menu.closest('.dropdown')?.classList.remove('open');
        }
    });
}

function renderFancySelect(selectId, options = {}) {
    const select = document.getElementById(selectId);
    if (!select) return;
    select.classList.add('native-hidden');

    const mount = options.mountId ? document.getElementById(options.mountId) : select.parentElement;
    if (!mount) return;

    const existing = mount.querySelector(`[data-fancy-for="${selectId}"]`);
    if (existing) existing.remove();

    fancyDropdowns[selectId] = fancyDropdowns[selectId] || {};
    const items = Array.from(select.options).map(opt => ({ value: opt.value, label: opt.textContent }));
    const chosen = select.value || items[0]?.value;
    if (!select.value && chosen) select.value = chosen;
    const searchTerm = (fancyDropdowns[selectId].search || '').toLowerCase();

    const dropdown = document.createElement('div');
    dropdown.className = `dropdown compact ${options.collapsible ? 'collapsible' : ''}`.trim();
    dropdown.dataset.fancyFor = selectId;

    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'dropdown-toggle';
    const toggleLabel = document.createElement('span');
    toggleLabel.textContent = items.find(i => i.value === select.value)?.label || options.placeholder || 'Нет вариантов';
    const chevron = document.createElement('span');
    chevron.className = 'chevron';
    chevron.textContent = '▾';
    toggle.append(toggleLabel, chevron);

    const menu = document.createElement('div');
    menu.className = 'dropdown-menu';

    if (!items.length) {
        menu.classList.add('empty');
        menu.textContent = options.placeholder || 'Список пуст';
    } else {
        if (options.searchable) {
            const searchWrap = document.createElement('div');
            searchWrap.className = 'dropdown-search';
            const search = document.createElement('input');
            search.type = 'search';
            search.placeholder = options.searchPlaceholder || 'Поиск...';
            search.value = fancyDropdowns[selectId].search || '';
            search.addEventListener('input', () => {
                fancyDropdowns[selectId].search = search.value;
                renderItems(search.value);
            });
            searchWrap.appendChild(search);
            menu.appendChild(searchWrap);
        }

        const listWrap = document.createElement('div');
        listWrap.className = 'dropdown-items';
        menu.appendChild(listWrap);

        const renderItems = (termRaw = '') => {
            const term = termRaw.toLowerCase();
            listWrap.innerHTML = '';
            const filteredItems = !options.searchable || !term ? items : items.filter(item => item.label.toLowerCase().includes(term));

            filteredItems.forEach(item => {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'dropdown-item';
                if (item.value === select.value) btn.classList.add('active');
                btn.innerHTML = `<strong>${item.label}</strong>`;
                btn.addEventListener('click', () => {
                    select.value = item.value;
                    toggleLabel.textContent = item.label;
                    select.dispatchEvent(new Event('change', { bubbles: true }));
                    closeFancyMenus();
                });
                listWrap.appendChild(btn);
            });
            if (!filteredItems.length) {
                const empty = document.createElement('div');
                empty.className = 'dropdown-empty muted';
                empty.textContent = 'Не найдено';
                listWrap.appendChild(empty);
            }
        };

        renderItems(searchTerm);
        menu.hidden = true;
    }

    toggle.addEventListener('click', () => {
        const isFolded = dropdown.classList.contains('folded');
        if (isFolded) {
            dropdown.classList.remove('folded');
            menu.hidden = false;
            dropdown.classList.add('open');
            return;
        }
        const next = menu.hidden;
        if (!next && options.collapsible) {
            menu.hidden = true;
            dropdown.classList.remove('open');
            dropdown.classList.add('folded');
            closeFancyMenus();
            return;
        }
        closeFancyMenus(dropdown);
        menu.hidden = !next ? true : false;
        dropdown.classList.toggle('open', next);
    });

    dropdown.appendChild(toggle);
    dropdown.appendChild(menu);
    mount.appendChild(dropdown);
    fancyDropdowns[selectId] = { dropdown, options };
}

function renderFancyForElement(select, options = {}) {
    if (!select) return;
    if (!select.id) select.id = `sel-${uid()}`;
    renderFancySelect(select.id, options);
}

function renderAllDropdowns() {
    renderFancySelect('simpleFunctionSelect', { mountId: 'simpleFunctionFancy', placeholder: 'Выберите функцию', collapsible: true, searchable: true });
    renderFancySelect('opFirst', { mountId: 'opFirstFancy', placeholder: 'Первая функция', collapsible: true, searchable: true });
    renderFancySelect('opSecond', { mountId: 'opSecondFancy', placeholder: 'Вторая функция', collapsible: true, searchable: true });
    renderFancySelect('diffSource', { mountId: 'diffSourceFancy', placeholder: 'Источник', collapsible: true, searchable: true });
    renderFancySelect('integralSource', { mountId: 'integralSourceFancy', placeholder: 'Интеграл', collapsible: true, searchable: true });
    renderFancySelect('saveSource', { mountId: 'saveSourceFancy', placeholder: 'Для сохранения', collapsible: true, searchable: true });
}

document.addEventListener('click', (event) => {
    const dropdown = event.target.closest('.dropdown');
    if (!dropdown) closeFancyMenus();
});

function renderChartPicker() {
    const container = document.getElementById('chartPicker');
    const select = document.getElementById('chartSource');
    if (!container || !select) return;
    select.classList.add('native-hidden');
    if (!select.value && state.functions[0]) {
        select.value = state.functions[0].id;
    }
    container.innerHTML = '';
    const selected = state.functions.find(fn => fn.id === select.value) || state.functions[0];

    const dropdown = document.createElement('div');
    dropdown.className = 'dropdown compact collapsible';

    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'dropdown-toggle';
    const toggleLabel = document.createElement('span');
    toggleLabel.textContent = selected ? selected.name : 'Нет функций';
    const chevron = document.createElement('span');
    chevron.className = 'chevron';
    chevron.textContent = '▾';
    toggle.append(toggleLabel, chevron);

    const menu = document.createElement('div');
    menu.className = 'dropdown-menu';
    if (!state.functions.length) {
        menu.classList.add('empty');
        menu.textContent = 'Нет функций для отображения';
    } else {
        const searchWrap = document.createElement('div');
        searchWrap.className = 'dropdown-search';
        const search = document.createElement('input');
        search.type = 'search';
        search.placeholder = 'Поиск по функциям';
        search.value = chartSearch;
        search.addEventListener('input', () => {
            chartSearch = search.value;
            renderItems(search.value);
        });
        searchWrap.appendChild(search);
        menu.appendChild(searchWrap);

        const listWrap = document.createElement('div');
        listWrap.className = 'dropdown-items';
        menu.appendChild(listWrap);

        const renderItems = (termRaw = '') => {
            const term = termRaw.toLowerCase();
            listWrap.innerHTML = '';
            const filtered = !term ? state.functions : state.functions.filter(fn => fn.name.toLowerCase().includes(term));

            filtered.forEach(fn => {
                const item = document.createElement('button');
                item.type = 'button';
                item.className = 'dropdown-item';
                if (select.value === fn.id) item.classList.add('active');
                item.innerHTML = `
                    <strong>${fn.name}</strong>
                    <small>${fn.points.length} точек</small>
                `;
                item.addEventListener('click', () => {
                    select.value = fn.id;
                    toggleLabel.textContent = fn.name;
                    menu.hidden = true;
                    dropdown.classList.remove('open');
                    drawChart();
                });
                listWrap.appendChild(item);
            });
            if (!filtered.length) {
                const empty = document.createElement('div');
                empty.className = 'dropdown-empty muted';
                empty.textContent = 'Не найдено';
                listWrap.appendChild(empty);
            }
        };

        renderItems(chartSearch);
        menu.hidden = true;
    }

    toggle.addEventListener('click', () => {
        if (!state.functions.length) return;
        const isFolded = dropdown.classList.contains('folded');
        if (isFolded) {
            dropdown.classList.remove('folded');
            menu.hidden = false;
            dropdown.classList.add('open');
            return;
        }
        const nextState = menu.hidden;
        if (!nextState) {
            menu.hidden = true;
            dropdown.classList.remove('open');
            dropdown.classList.add('folded');
            closeFancyMenus();
            return;
        }
        closeFancyMenus(dropdown);
        menu.hidden = !nextState ? true : false;
        dropdown.classList.toggle('open', nextState);
    });

    if (!chartDropdownListenerBound) {
        chartDropdownListenerBound = true;
        document.addEventListener('click', (event) => {
            const picker = document.getElementById('chartPicker');
            if (picker && !picker.contains(event.target)) {
                picker.querySelectorAll('.dropdown-menu').forEach(m => m.hidden = true);
                picker.querySelectorAll('.dropdown').forEach(d => d.classList.remove('open'));
            }
        });
    }

    dropdown.appendChild(toggle);
    dropdown.appendChild(menu);
    container.appendChild(dropdown);
}

function pushFunction(name, points, meta = {}) {
    const duplicate = findDuplicate(points, name);
    if (duplicate) {
        document.getElementById('chartSource').value = duplicate.id;
        renderChartPicker();
        drawChart();
        return duplicate;
    }
    const fn = {
        id: meta.id ?? uid(),
        name,
        factory: state.factory,
        points,
        insertable: meta.insertable ?? true,
        removable: meta.removable ?? true,
        persisted: meta.persisted ?? false,
    };
    state.functions.push(fn);
    syncFunctionSelects();
    drawChart();
    persistLocalFunctions();
    return fn;
}

async function persistFunction(name, points, meta = {}) {
    const duplicate = findDuplicate(points, name);
    if (duplicate) {
        document.getElementById('chartSource').value = duplicate.id;
        renderChartPicker();
        drawChart();
        return duplicate;
    }
    const user = await ensureAuthUser();
    if (!user) {
        return pushFunction(name, points, { persisted: false, ...meta });
    }

    try {
        const func = await api.createFunction({ name, type: 'TABULATED', ownerId: user.id });
        const functionId = func?.id ?? func?.functionId ?? func?.function?.id;

        try {
            await api.createPoints({ xValues: points.map(p => p.x), yValues: points.map(p => p.y), functionId: functionId || func.id });
        }
        catch (err) {
            return pushFunction(name, points, { persisted: false, ...meta });
        }

        return pushFunction(func.name || name, points, { id: functionId || uid(), persisted: true, ...meta });
    }

    catch (error) {
        return pushFunction(name, points, { persisted: false, ...meta });
    }
}

async function createFunctionFromPoints() {
    const name = document.getElementById('pointsName').value.trim() || `f${state.functions.length + 1}`;
    const points = gatherPoints();
    if (!points) return;
    try {
        await persistFunction(name, points);
        showModal('Готово', `Функция «${name}» создана и сохранена (${points.length} точек).`);
    }

    catch (error) {
        showModal('Ошибка сохранения', error.message);
    }
}

bind('buildPoints', 'click', createFunctionFromPoints);

function refreshSimpleList() {
    const select = document.getElementById('simpleFunctionSelect');
    select.innerHTML = '';
    simpleFunctions
        .slice()
        .sort((a, b) => a.label.localeCompare(b.label))
        .forEach(item => {
            const option = document.createElement('option');
            option.value = item.key;
            option.textContent = item.label;
            select.appendChild(option);
        });
    renderSimpleParams();
    renderSimpleGallery();
    renderAllDropdowns();
}

bind('refreshSimpleFunctions', 'click', refreshSimpleList);
refreshSimpleList();

function renderSimpleParams() {
    const container = document.getElementById('simpleParams');
    const key = document.getElementById('simpleFunctionSelect').value;
    const def = simpleFunctions.find(i => i.key === key);
    container.innerHTML = '';
    if (!def?.params) return;
    def.params.forEach(param => {
        const wrapper = document.createElement('label');
        wrapper.textContent = param.label;
        const input = document.createElement('input');
        input.type = param.type || 'text';
        input.value = param.default ?? '';
        input.dataset.paramKey = param.key;
        wrapper.appendChild(input);
        container.appendChild(wrapper);
    });
}
bind('simpleFunctionSelect', 'change', renderSimpleParams);

function createFunctionFromSimple() {
    const key = document.getElementById('simpleFunctionSelect').value;
    const def = simpleFunctions.find(i => i.key === key);
    const name = document.getElementById('simpleName').value.trim() || def.label;
    const parts = document.getElementById('simpleRange').value.split(';');
    const count = parseInt(document.getElementById('simplePoints').value, 10);
    const from = parseFloat(parts[0]);
    const to = parseFloat(parts[1]);
    if (!def || Number.isNaN(from) || Number.isNaN(to) || count < 2) {
        showModal('Ошибка', 'Проверьте интервал и количество точек.');
        return;
    }
    const step = (to - from) / (count - 1);
    const params = {};
    document.querySelectorAll('#simpleParams [data-param-key]').forEach(input => {
        params[input.dataset.paramKey] = input.value;
    });
    const mathFn = def.build ? def.build(params) : (x) => x;
    const points = Array.from({ length: count }, (_, i) => {
        const x = from + step * i;
        return { x, y: mathFn(x) };
    });
    persistFunction(name, points)
        .then(() => showModal('Готово', `Функция «${name}» создана из ${def.label}.`))
        .catch(error => showModal('Ошибка сохранения', error.message));
}

bind('buildSimple', 'click', createFunctionFromSimple);

function binaryOp(op) {
    const first = state.functions.find(f => f.id === document.getElementById('opFirst').value);
    const second = state.functions.find(f => f.id === document.getElementById('opSecond').value);
    if (!first || !second) return showModal('Ошибка', 'Нужно выбрать обе функции.');

    if (state.auth?.user && first.persisted && second.persisted) {
        const resultName = `${first.name} ${op} ${second.name}`;
        api.operate({
            operation: op,
            firstFunctionId: first.id,
            secondFunctionId: second.id,
            factory: state.factory,
            resultName,
        })
            .then(response => {
                const points = pointsFromResponse(response);
                pushFunction(resultName, points, {
                    id: response.functionId ?? uid(),
                    persisted: Boolean(response.functionId),
                });
                document.getElementById('opResult').textContent = `Результат (сервер): ${resultName} (${points.length} точек)`;
            })
            .catch(error => showModal('Ошибка операции', error.message));
        return;
    }

    const length = Math.min(first.points.length, second.points.length);
    const resultPoints = [];
    for (let i = 0; i < length; i++) {
        const x = first.points[i].x;
        const y1 = first.points[i].y;
        const y2 = second.points[i].y;
        let y;
        switch (op) {
            case 'add': y = y1 + y2; break;
            case 'sub': y = y1 - y2; break;
            case 'mul': y = y1 * y2; break;
            case 'div': y = y2 === 0 ? NaN : y1 / y2; break;
            default: y = 0;
        }
        resultPoints.push({ x, y });
    }
    const name = `${first.name} ${op} ${second.name}`;
    persistFunction(name, resultPoints, { derived: true })
        .then(() => document.getElementById('opResult').textContent = `Результат: ${name} (${resultPoints.length} точек)`)
        .catch(error => showModal('Ошибка операции', error.message));
}

document.querySelectorAll('[data-op]').forEach(btn => {
    btn.addEventListener('click', () => binaryOp(btn.dataset.op));
});

bind('insertBtn', 'click', () => {
    const target = state.functions.find(f => f.id === document.getElementById('opFirst').value);
    if (!target) return showModal('Ошибка', 'Выберите функцию для вставки.');
    if (!target.insertable) return showModal('Запрет', 'Функция не поддерживает Insertable.');
    const x = parseFloat(prompt('Введите X новой точки', '0'));
    const y = parseFloat(prompt('Введите Y новой точки', '0'));
    if (Number.isNaN(x) || Number.isNaN(y)) return;
    target.points.push({ x, y });
    target.points.sort((a, b) => a.x - b.x);
    syncFunctionSelects();
    drawChart();
    persistLocalFunctions();
    showModal('Вставлено', 'Точка добавлена.');
});

bind('removeBtn', 'click', () => {
    const target = state.functions.find(f => f.id === document.getElementById('opSecond').value);
    if (!target) return showModal('Ошибка', 'Выберите функцию для удаления.');
    if (!target.removable) return showModal('Запрет', 'Функция не поддерживает Removable.');
    target.points.pop();
    syncFunctionSelects();
    drawChart();
    persistLocalFunctions();
    showModal('Удалено', 'Последняя точка удалена.');
});

function differentiate() {
    const source = state.functions.find(f => f.id === document.getElementById('diffSource').value);
    if (!source) return showModal('Ошибка', 'Выберите функцию.');

    const resultName = `${source.name}'`;
    if (state.auth?.user && source.persisted) {
        api.operate({
            operation: 'diff',
            firstFunctionId: source.id,
            factory: state.factory,
            resultName,
        })
            .then(response => {
                const points = pointsFromResponse(response);
                pushFunction(resultName, points, {
                    id: response.functionId ?? uid(),
                    persisted: Boolean(response.functionId),
                });
                document.getElementById('diffResult').textContent = `Создана производная (сервер): ${resultName}`;
            })
            .catch(error => showModal('Ошибка сохранения', error.message));
        return;
    }

    const result = [];
    for (let i = 0; i < source.points.length - 1; i++) {
        const x0 = source.points[i].x;
        const x1 = source.points[i + 1].x;
        const y0 = source.points[i].y;
        const y1 = source.points[i + 1].y;
        const dx = x1 - x0;
        result.push({ x: x0, y: dx === 0 ? NaN : (y1 - y0) / dx });
    }
    persistFunction(`${source.name}'`, result, { derived: true })
        .then(fn => document.getElementById('diffResult').textContent = `Создана производная: ${fn.name}`)
        .catch(error => showModal('Ошибка сохранения', error.message));
}

bind('diffBtn', 'click', differentiate);

function integrate() {
    const source = state.functions.find(f => f.id === document.getElementById('integralSource').value);
    if (!source) return showModal('Ошибка', 'Выберите функцию.');

    const threads = Math.min(parseInt(document.getElementById('integralThreads').value, 10) || 1, 16);
    if (state.auth?.user && source.persisted) {
        return api.operate({
            operation: 'integral',
            firstFunctionId: source.id,
            threads,
            factory: state.factory,
        })
            .then(response => {
                const value = Number(response.value ?? 0);
                document.getElementById('integralResult').textContent = `Интеграл ≈ ${value.toFixed(4)} (сервер, потоков: ${threads})`;
                return value;
            })
            .catch(error => {
                showModal('Ошибка интегрирования', error.message);
                return undefined;
            });
    }

    let area = 0;
    for (let i = 0; i < source.points.length - 1; i++) {
        const a = source.points[i];
        const b = source.points[i + 1];
        area += ((a.y + b.y) / 2) * (b.x - a.x);
    }
    document.getElementById('integralResult').textContent = `Интеграл ≈ ${area.toFixed(4)} (потоков: ${threads})`;
    return area;
}

bind('integralBtn', 'click', integrate);

function applyValue() {
    const source = state.functions.find(f => f.id === document.getElementById('chartSource').value);
    if (!source) return showModal('Ошибка', 'Выберите функцию.');
    const x = parseFloat(document.getElementById('applyX').value);

    if (state.auth?.user && source.persisted) {
        return api.operate({
            operation: 'apply',
            firstFunctionId: source.id,
            applyX: x,
            factory: state.factory,
        })
            .then(response => {
                const value = Number(response.value ?? 0);
                document.getElementById('applyResult').textContent = `apply(${x}) = ${value.toFixed(4)} (сервер)`;
                return value;
            })
            .catch(error => {
                showModal('Ошибка вычисления', error.message);
                return undefined;
            });
    }

    const sorted = source.points.slice().sort((a, b) => a.x - b.x);
    const nearest = findClosestPoint(sorted, x) ?? sorted[0];
    document.getElementById('applyResult').textContent = `apply(${x}) ≈ ${nearest.y.toFixed(4)} (ближайшая точка)`;
    return nearest.y;
}

bind('applyBtn', 'click', applyValue);

function downsampleForCanvas(points, width) {
    const target = Math.max(10, Math.min(points.length, Math.floor(width * 2)));
    if (points.length <= target) return points;

    const bucketSize = Math.ceil(points.length / Math.max(1, target / 2));
    let result = [];
    for (let i = 0; i < points.length; i += bucketSize) {
        const slice = points.slice(i, i + bucketSize);
        if (!slice.length) continue;
        let min = slice[0], max = slice[0];
        for (const p of slice) {
            if (p.y < min.y) min = p;
            if (p.y > max.y) max = p;
        }
        result.push(slice[0]);
        if (min !== slice[0]) result.push(min);
        if (max !== slice[0] && max !== min) result.push(max);
    }
    result.push(points[points.length - 1]);

    const dedup = [];
    result
        .sort((a, b) => a.x - b.x)
        .forEach(p => {
            if (!dedup.length || dedup[dedup.length - 1].x !== p.x) dedup.push(p);
            else dedup[dedup.length - 1] = p;
        });

    const cap = Math.min(2000, Math.max(target, 1200));
    while (dedup.length > cap) {
        const keep = [dedup[0]];
        for (let i = 1; i < dedup.length - 1; i += 2) keep.push(dedup[i]);
        keep.push(dedup[dedup.length - 1]);
        dedup.length = 0;
        dedup.push(...keep);
    }
    return dedup;
}

function niceTicks(min, max, target = 8) {
    const span = max - min || 1;
    const step0 = span / Math.max(1, target);
    const mag = Math.pow(10, Math.floor(Math.log10(step0)));
    const norm = step0 / mag;
    const niceNorm = norm >= 5 ? 10 : norm >= 2 ? 5 : norm >= 1 ? 2 : 1;
    const step = niceNorm * mag;
    const start = Math.floor(min / step) * step;
    const end = Math.ceil(max / step) * step;
    const ticks = [];
    for (let v = start; v <= end + 1e-9; v += step) ticks.push(v);
    return ticks;
}

function formatTick(v) {
    const abs = Math.abs(v);
    if (!isFinite(v)) return '';
    if ((abs >= 1e4 || abs < 1e-3) && abs !== 0) return v.toExponential(1);
    return Math.abs(v) >= 100 ? v.toFixed(0) : v.toFixed(2);
}

function findClosestPoint(sortedPoints, targetX) {
    if (!sortedPoints.length) return null;
    let l = 0, r = sortedPoints.length - 1;
    while (r - l > 1) {
        const m = Math.floor((l + r) / 2);
        if (sortedPoints[m].x > targetX) r = m; else l = m;
    }
    const candL = sortedPoints[l];
    const candR = sortedPoints[Math.min(sortedPoints.length - 1, l + 1)];
    return Math.abs(candL.x - targetX) <= Math.abs(candR.x - targetX) ? candL : candR;
}

function drawChart() {
    const canvas = document.getElementById('chart');
    if (!canvas) return;
    const wrap = document.querySelector('.chart-wrap');
    const ratio = window.devicePixelRatio || 1;
    const height = canvas.clientHeight || 360;
    const pad = 48;

    const source = state.functions.find(f => f.id === document.getElementById('chartSource').value);
    const overlayCtx = chartOverlay?.getContext('2d');
    if (overlayCtx) overlayCtx.clearRect(0, 0, chartOverlay.width || 0, chartOverlay.height || 0);
    if (!source || !source.points.length) {
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        tooltipEl.hidden = true;
        return;
    }

    const sorted = source.points.slice().sort((a, b) => a.x - b.x);
    const dataXs = sorted.map(p => p.x);
    const dataYs = sorted.map(p => p.y);
    let rawMinX = Math.min(...dataXs);
    let rawMaxX = Math.max(...dataXs);
    let rawMinY = Math.min(...dataYs);
    let rawMaxY = Math.max(...dataYs);

    if (!Number.isFinite(rawMinX) || !Number.isFinite(rawMaxX) || !Number.isFinite(rawMinY) || !Number.isFinite(rawMaxY)) {
        showModal('Ошибка данных', 'В наборе точек есть некорректные значения. Проверьте X и Y.');
        return;
    }

    if (rawMinX === rawMaxX) { rawMinX -= 1; rawMaxX += 1; }
    if (rawMinY === rawMaxY) { rawMinY -= 1; rawMaxY += 1; }

    const xRange = parseRange(document.getElementById('chartRangeX').value);
    const yRange = parseRange(document.getElementById('chartRangeY').value);

    let minX = xRange?.min ?? rawMinX;
    let maxX = xRange?.max ?? rawMaxX;
    let minY = yRange?.min ?? rawMinY;
    let maxY = yRange?.max ?? rawMaxY;

    if (maxX <= minX) { minX = rawMinX; maxX = rawMaxX; }
    if (maxY <= minY) { minY = rawMinY; maxY = rawMaxY; }
    if (minX === maxX) { minX -= 1; maxX += 1; }
    if (minY === maxY) { minY -= 1; maxY += 1; }

    const visible = sorted.filter(p => p.x >= minX && p.x <= maxX);
    const renderPool = visible.length ? visible : sorted;
    const baseWidth = wrap?.clientWidth || canvas.clientWidth || 900;
    const desiredWidth = baseWidth;
    canvas.style.width = `${desiredWidth}px`;
    if (chartOverlay) chartOverlay.style.width = `${desiredWidth}px`;

    const width = desiredWidth;
    canvas.width = width * ratio;
    canvas.height = height * ratio;
    const ctx = canvas.getContext('2d');
    ctx.save();
    ctx.scale(ratio, ratio);
    ctx.clearRect(0, 0, width, height);

    if (chartOverlay) {
        chartOverlay.width = width * ratio;
        chartOverlay.height = height * ratio;
        chartOverlay.getContext('2d').setTransform(ratio, 0, 0, ratio, 0, 0);
        chartOverlay.getContext('2d').clearRect(0, 0, width, height);
    }

    const sampled = downsampleForCanvas(renderPool, width);

    const spanX = (maxX - minX) || 1e-6;
    const spanY = (maxY - minY) || 1e-6;
    const innerW = width - pad * 2;
    const innerH = height - pad * 2;
    const mapX = (x) => ((x - minX) / spanX) * innerW + pad;
    const mapY = (y) => height - (((y - minY) / spanY) * innerH + pad);

    state.chartView = { mapX, mapY, minX, maxX, minY, maxY, spanX, spanY, pad, width, height, visible: visible.length ? visible : sorted };

    // сетка и оси
    const ticks = niceTicks(minX, maxX);
    const yTicks = niceTicks(minY, maxY);
    ctx.strokeStyle = getComputedStyle(document.body).getPropertyValue('--canvas-grid');
    ctx.lineWidth = 1;
    ticks.forEach((v) => {
        const gx = mapX(v);
        ctx.beginPath();
        ctx.moveTo(gx, pad);
        ctx.lineTo(gx, height - pad);
        ctx.stroke();
    });
    yTicks.forEach((v) => {
        const gy = mapY(v);
        ctx.beginPath();
        ctx.moveTo(pad, gy);
        ctx.lineTo(width - pad, gy);
        ctx.stroke();
    });

    ctx.strokeStyle = getComputedStyle(document.body).getPropertyValue('--canvas-axis');
    ctx.lineWidth = 1.5;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(pad, height - pad);
    ctx.lineTo(width - pad, height - pad);
    ctx.moveTo(pad, height - pad);
    ctx.lineTo(pad, pad);
    ctx.stroke();

    if (minY < 0 && maxY > 0) {
        ctx.strokeStyle = 'rgba(99,102,241,0.6)';
        ctx.setLineDash([6, 6]);
        ctx.beginPath();
        ctx.moveTo(pad, mapY(0));
        ctx.lineTo(width - pad, mapY(0));
        ctx.stroke();
        ctx.setLineDash([]);
    }
    if (minX < 0 && maxX > 0) {
        ctx.strokeStyle = 'rgba(99,102,241,0.6)';
        ctx.setLineDash([6, 6]);
        ctx.beginPath();
        ctx.moveTo(mapX(0), pad);
        ctx.lineTo(mapX(0), height - pad);
        ctx.stroke();
        ctx.setLineDash([]);
    }

    ctx.fillStyle = getComputedStyle(document.body).getPropertyValue('--muted');
    ctx.font = '12px Inter, system-ui';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ticks.forEach(v => {
        const gx = mapX(v);
        ctx.fillText(formatTick(v), gx, height - pad + 6);
    });
    ctx.textAlign = 'right';
    ctx.textBaseline = 'middle';
    yTicks.forEach(v => {
        const gy = mapY(v);
        ctx.fillText(formatTick(v), pad - 8, gy);
    });
    ctx.save();
    ctx.fillStyle = getComputedStyle(document.body).getPropertyValue('--muted');
    ctx.textAlign = 'right';
    ctx.fillText('Y', pad - 14, pad - 8);
    ctx.textAlign = 'center';
    ctx.fillText('X', width - pad + 12, height - pad + 16);
    ctx.restore();

    ctx.save();
    ctx.beginPath();
    ctx.rect(pad, pad, innerW, innerH);
    ctx.clip();
    ctx.strokeStyle = '#2dd4bf';
    ctx.lineWidth = 2;
    ctx.beginPath();
    sampled.forEach((p, i) => {
        const cx = mapX(p.x);
        const cy = mapY(p.y);
        if (i === 0) ctx.moveTo(cx, cy); else ctx.lineTo(cx, cy);
    });
    ctx.stroke();

    if (sampled.length <= 4000) {
        ctx.fillStyle = '#60a5fa';
        sampled.forEach(p => {
            ctx.beginPath();
            ctx.arc(mapX(p.x), mapY(p.y), 3, 0, Math.PI * 2);
            ctx.fill();
        });
    }
    ctx.restore();

    const searchPoints = visible.length ? visible : sorted;
    canvas.onmousemove = e => {
        const view = state.chartView;
        const rect = canvas.getBoundingClientRect();
        const xPixel = e.clientX - rect.left;
        const normalized = Math.min(Math.max(xPixel - pad, 0), innerW) / innerW;
        const targetX = view.minX + normalized * view.spanX;
        const closest = findClosestPoint(searchPoints, targetX);
        if (closest) {
            const cx = mapX(closest.x);
            const cy = mapY(closest.y);
            tooltipEl.style.left = `${e.pageX + 10}px`;
            tooltipEl.style.top = `${e.pageY + 10}px`;
            tooltipEl.textContent = `(${closest.x.toFixed(4)}; ${closest.y.toFixed(4)})`;
            tooltipEl.hidden = false;

            if (overlayCtx && chartOverlay) {
                overlayCtx.clearRect(0, 0, width * ratio, height * ratio);
                overlayCtx.save();
                overlayCtx.scale(ratio, ratio);
                overlayCtx.setLineDash([5, 5]);
                overlayCtx.strokeStyle = 'rgba(99,102,241,0.6)';
                overlayCtx.lineWidth = 1;
                overlayCtx.beginPath();
                overlayCtx.moveTo(cx, pad);
                overlayCtx.lineTo(cx, height - pad);
                overlayCtx.moveTo(pad, cy);
                overlayCtx.lineTo(width - pad, cy);
                overlayCtx.stroke();
                overlayCtx.restore();
            }
        } else {
            tooltipEl.hidden = true;
            overlayCtx?.clearRect(0, 0, width * ratio, height * ratio);
        }
    };
    canvas.onmouseleave = () => {
        tooltipEl.hidden = true;
        chartOverlay?.getContext('2d')?.clearRect(0, 0, width * ratio, height * ratio);
    };
    ctx.restore();
}

bind('chartRedraw', 'click', drawChart);
bind('chartAuto', 'click', () => {
    const source = state.functions.find(f => f.id === document.getElementById('chartSource').value);
    if (!source?.points?.length) return;
    const xs = source.points.map(p => p.x);
    const ys = source.points.map(p => p.y);
    document.getElementById('chartRangeX').value = `${Math.min(...xs)};${Math.max(...xs)}`;
    document.getElementById('chartRangeY').value = `${Math.min(...ys)};${Math.max(...ys)}`;
    drawChart();
});
bind('chartPickerCollapse', 'click', () => {
    const wrap = document.getElementById('chartPickerWrap');
    if (!wrap) return;
    const collapsed = wrap.classList.toggle('collapsed');
    const btn = document.getElementById('chartPickerCollapse');
    if (btn) btn.textContent = collapsed ? 'Показать список' : 'Свернуть список';
});

window.addEventListener('resize', () => {
    if (state.functions.length) {
        drawChart();
    }
});

function syncComposite() {
    const container = document.getElementById('compositeSteps');
    container.querySelectorAll('select[data-kind="fn"]').forEach(sel => {
        sel.innerHTML = '';
        state.functions.forEach(fn => {
            const option = document.createElement('option');
            option.value = fn.id;
            option.textContent = fn.name;
            sel.appendChild(option);
        });
        renderFancyForElement(sel, { placeholder: 'Шаг' });
    });
    container.querySelectorAll('select[data-kind="op"]').forEach(sel => renderFancyForElement(sel, { placeholder: 'Операция' }));
}

bind('addCompositeStep', 'click', () => {
    const div = document.createElement('div');
    div.className = 'card muted';
    div.innerHTML = `
        <label>Функция<select data-kind="fn"></select></label>
        <label>Операция<select data-kind="op">
            <option value="identity">Без изменения</option>
            <option value="diff">Производная</option>
            <option value="double">Удвоить значения</option>
        </select></label>
    `;
    document.getElementById('compositeSteps').appendChild(div);
    syncComposite();
});

bind('buildComposite', 'click', () => {
    const steps = Array.from(document.querySelectorAll('#compositeSteps div'));
    if (steps.length === 0) return showModal('Ошибка', 'Добавьте хотя бы один шаг.');
    let resultPoints = null;
    steps.forEach(step => {
        const fnId = step.querySelector('[data-kind="fn"]').value;
        const op = step.querySelector('[data-kind="op"]').value;
        const fn = state.functions.find(f => f.id === fnId);
        if (!fn) return;
        let current = fn.points.map(p => ({ ...p }));
        if (op === 'diff') {
            current = current.slice(0, -1).map((p, i) => {
                const next = fn.points[i + 1];
                return { x: p.x, y: (next.y - p.y) / (next.x - p.x) };
            });
        } else if (op === 'double') {
            current = current.map(p => ({ x: p.x, y: p.y * 2 }));
        }
        resultPoints = resultPoints ? resultPoints.map((p, idx) => ({ x: p.x, y: p.y + (current[idx]?.y ?? 0) })) : current;
    });
    const name = `Composite-${uid().slice(0, 4)}`;
    persistFunction(name, resultPoints || [])
        .then(fn => document.getElementById('compositeResult').textContent = `Создана сложная функция: ${fn.name}`)
        .catch(error => showModal('Ошибка сохранения', error.message));
});

function saveFunction() {
    const source = state.functions.find(f => f.id === document.getElementById('saveSource').value);
    if (!source) return showModal('Ошибка', 'Выберите функцию.');
    const format = document.getElementById('saveFormat').value;
    let blob;
    if (format === 'json') {
        blob = new Blob([JSON.stringify(source, null, 2)], { type: 'application/json' });
    } else {
        const xml = `<function name="${source.name}" factory="${source.factory}">` +
            source.points.map(p => `<point x="${p.x}" y="${p.y}"/>`).join('') + '</function>';
        blob = new Blob([xml], { type: 'application/xml' });
    }
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${source.name}.${format}`;
    a.click();
    URL.revokeObjectURL(url);
    document.getElementById('fileResult').textContent = `Сохранено: ${a.download}`;
}

bind('saveBtn', 'click', saveFunction);

const loadFileInput = document.getElementById('loadFile');
const fileNameLabel = document.getElementById('fileName');
if (loadFileInput) {
    loadFileInput.addEventListener('change', () => {
        const name = loadFileInput.files?.[0]?.name || 'Файл не выбран';
        if (fileNameLabel) fileNameLabel.textContent = name;
    });
}

bind('loadBtn', 'click', () => {
    if (!loadFileInput?.files?.length) return showModal('Ошибка', 'Выберите файл.');
    const file = loadFileInput.files[0];
    const reader = new FileReader();
    reader.onload = () => {
        let name = file.name.replace(/\.[^.]+$/, '');
        let points = [];
        if (file.name.endsWith('.json')) {
            const data = JSON.parse(reader.result);
            points = data.points || [];
            name = data.name || name;
        } else {
            const parser = new DOMParser();
            const doc = parser.parseFromString(reader.result, 'application/xml');
            points = Array.from(doc.querySelectorAll('point')).map(p => ({
                x: parseFloat(p.getAttribute('x')),
                y: parseFloat(p.getAttribute('y')),
            }));
        }
        persistFunction(name, points)
            .then(() => document.getElementById('fileResult').textContent = `Загружено: ${name}`)
            .catch(error => showModal('Ошибка сохранения', error.message));
    };
    reader.readAsText(file);
});

bind('syncBtn', 'click', syncFromServer);

function setupCollapsibles() {
    const cards = Array.from(document.querySelectorAll('[data-mobile-collapse]'));
    const media = window.matchMedia('(max-width: 768px)');

    const apply = () => {
        cards.forEach((card, idx) => {
            const body = card.querySelector('.card-body');
            if (!body) return;
            if (!media.matches) {
                card.classList.remove('collapsed');
                body.hidden = false;
                return;
            }
            if (!card.dataset.initCollapse) {
                const collapseByDefault = idx > 1;
                card.classList.toggle('collapsed', collapseByDefault);
                body.hidden = collapseByDefault;
                card.dataset.initCollapse = 'true';
            } else {
                body.hidden = card.classList.contains('collapsed');
            }
        });
    };

    cards.forEach(card => {
        const body = card.querySelector('.card-body');
        const toggle = card.querySelector('.collapse-toggle');
        if (!body || !toggle) return;
        toggle.addEventListener('click', () => {
            const collapsed = card.classList.toggle('collapsed');
            if (media.matches) body.hidden = collapsed; else body.hidden = false;
        });
    });

    media.addEventListener('change', apply);
    apply();
}

function seedDefaults() {
    addPointRow(0, 0);
    addPointRow(1, 1);
    updateAuthBadge();
    const restored = restoreLocalFunctions();
    if (!restored) {
        bootstrapDemoFunctions();
    }
    if (state.auth && !state.offline) {
        flushPendingToServer().then(syncFromServer);
    }
}

ready(() => {
    seedDefaults();
    setupCollapsibles();
});

// API для тестов
window.AppAPI = {
    gatherPoints,
    integrate,
    binaryOp,
    applyValue,
    pushFunction,
    downsampleForCanvas,
    niceTicks,
    formatTick,
    state,
};

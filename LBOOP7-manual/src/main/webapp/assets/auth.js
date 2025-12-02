const api = {
    async request(path, options = {}) {
        const response = await fetch(path, {
            ...options,
            headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        });
        if (!response.ok) {
            let message = response.statusText;
            try {
                const parsed = await response.json();
                message = parsed.message || message;
            } catch (_) {
                const text = await response.text();
                if (text) message = text;
            }
            const error = new Error(message || 'Ошибка запроса');
            error.status = response.status;
            throw error;
        }
        return response.status === 204 ? null : response.json();
    },
    async createUser(payload) {
        return this.request('/users', { method: 'POST', body: JSON.stringify(payload) });
    },
    async fetchUser(username, credentials) {
        return this.request(`/users?username=${encodeURIComponent(username)}`, {
            headers: { Authorization: `Basic ${credentials}` },
        });
    },
};

function setMessage(text, isError = false) {
    const el = document.getElementById('authMessage');
    el.textContent = text;
    el.style.color = isError ? '#fca5a5' : 'var(--text)';
}

function validatePassword(raw, confirm) {
    if (!raw) return 'Введите пароль';
    if (confirm !== undefined && confirm !== null && confirm !== '' && raw !== confirm) {
        return 'Пароли не совпадают';
    }
    if (raw.length < 8) return 'Пароль должен быть не короче 8 символов';
    if (!/[A-ZА-Я]/.test(raw)) return 'Добавьте хотя бы одну заглавную букву';
    if (!/[a-zа-я]/.test(raw)) return 'Добавьте хотя бы одну строчную букву';
    if (!/\d/.test(raw)) return 'Добавьте хотя бы одну цифру';
    return null;
}

async function login() {
    const username = document.getElementById('authUsername').value.trim();  // ← ИЗМЕНИТЬ ID
    const password = document.getElementById('authPassword').value;
    if (!username || !password) return setMessage('Введите имя и пароль', true);
    const credentials = btoa(`${username}:${password}`);
    try {
        const user = await api.fetchUser(username, credentials);
        localStorage.setItem('lab7Auth', JSON.stringify({ username, credentials, user }));
        localStorage.removeItem('lab7Offline');
        setMessage('Успех! Перенаправляю...');
        window.location.href = '/index.html';
    } catch (err) {
        const msg = err.status === 401 || err.message?.includes('WWW-Authenticate')
            ? 'Проверьте логин/пароль'
            : err.status === 503 ? 'База данных недоступна — запустите контейнер postgres или задайте переменные DB_*'
            : err.message;
        setMessage(msg || 'Не удалось войти', true);
    }
}

async function registerUser() {
    const username = document.getElementById('authUsername').value.trim();
    const password = document.getElementById('authPassword').value;
    const confirm = document.getElementById('authConfirm').value;
    if (!username || !password) return setMessage('Заполните имя и пароль', true);
    const passwordError = validatePassword(password, confirm);
    if (passwordError) return setMessage(passwordError, true);
    try {
        const created = await api.createUser({
            username: username,
            passwordHash: password,
            email: '',
            confirmPassword: confirm,
        });
        const credentials = btoa(`${username}:${password}`);
        localStorage.setItem('lab7Auth', JSON.stringify({ username, credentials, user: created }));
        localStorage.removeItem('lab7Offline');
        // проверяем, что аутентификация сразу работает
        try {
            await api.fetchUser(username, credentials);
        } catch (e) {
            setMessage('Учётная запись создана, но не удалось войти: ' + (e.message || ''), true);
            return;
        }
        setMessage('Учётная запись создана! Перенаправляю...');
        window.location.href = '/index.html';
    } catch (err) {
        if (err.status === 409) {
            setMessage('Такой логин уже занят — выберите другой', true);
            return;
        }
        if (err.status === 503) {
            setMessage('База данных недоступна — убедитесь, что контейнер postgres запущен', true);
            return;
        }
        const msg = err.message?.includes('Failed to create user')
            ? 'Не удалось создать учётную запись. Проверьте подключение к БД и повторите.'
            : err.message || 'Не удалось зарегистрировать';
        setMessage(msg, true);
    }
}

function goOffline() {
    localStorage.removeItem('lab7Auth');
    localStorage.setItem('lab7Offline', '1');
    window.location.href = '/index.html';
}

document.getElementById('authLogin').addEventListener('click', login);
const regBtn = document.getElementById('authRegister');
if (regBtn) regBtn.addEventListener('click', registerUser);
const offBtn = document.getElementById('authOffline');
if (offBtn) offBtn.addEventListener('click', goOffline);

document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        login();
    }
});

(() => {
    const log = document.getElementById('diceLog');
    const portal = document.getElementById('arcanePortal');
    const portalValue = document.getElementById('portalValue');
    const diceReadout = document.getElementById('diceReadout');
    const rerollBtn = document.getElementById('rerollD20');
    const lootBtn = document.getElementById('lootBtn');
    const lootList = document.getElementById('lootList');
    const rumorBtn = document.getElementById('rumorBtn');
    const rumorText = document.getElementById('rumorText');
    const questBtn = document.getElementById('questBtn');
    const questText = document.getElementById('questText');
    const npcBtn = document.getElementById('npcBtn');
    const npcText = document.getElementById('npcText');
    const startBtn = document.getElementById('adventureStart');
    const attackBtn = document.getElementById('adventureAttack');
    const defendBtn = document.getElementById('adventureDefend');
    const spellBtn = document.getElementById('adventureSpell');
    const restBtn = document.getElementById('adventureRest');
    const gameLog = document.getElementById('gameLog');
    const gameStats = document.getElementById('gameStats');
    const skillBtn = document.getElementById('skillRoll');
    const skillDc = document.getElementById('skillDc');
    const skillSelect = document.getElementById('skillSelect');
    const skillResult = document.getElementById('skillResult');
    const arenaButtons = Array.from(document.querySelectorAll('[data-arena]'));
    const arenaLog = document.getElementById('arenaLog');
    const wheelSpin = document.getElementById('wheelSpin');
    const wheelResult = document.getElementById('wheelResult');
    const raceBtn = document.getElementById('raceBtn');
    const cardBtn = document.getElementById('cardGame');
    const riddleBtn = document.getElementById('riddleBtn');
    const brewBtn = document.getElementById('brewBtn');
    const miniGameBoard = document.getElementById('miniGameBoard');
    const bunkerDealBtn = document.getElementById('bunkerDeal');
    const bunkerVoteBtn = document.getElementById('bunkerVote');
    const bunkerBoard = document.getElementById('bunkerBoard');
    const bunkerSelect = document.getElementById('bunkerSelect');
    const bunkerFactSelect = document.getElementById('bunkerFact');
    const bunkerRevealBtn = document.getElementById('bunkerReveal');
    const bunkerSpecialBtn = document.getElementById('bunkerSpecial');
    const bunkerSaveBtn = document.getElementById('bunkerSave');
    const bunkerEventBtn = document.getElementById('bunkerEvent');
    const bunkerResetBtn = document.getElementById('bunkerReset');
    const bunkerLog = document.getElementById('bunkerLog');

    const gameState = {
        hero: null,
        foe: null,
        guard: false,
        sparks: 1,
    };

    let lastRoll = null;
    let secretUnlocked = false;

    const lootTable = [
        'Алебарда гнева', 'Зелье невидимости', 'Сапоги эльфа-следопыта',
        'Амулет сопротивления огню', 'Посох призыва элементаля', 'Свиток телепортации',
        'Сумка хранения', 'Кольцо защиты +1', 'Коготь белого дракона', 'Мешочек с драконьими костями'
    ];

    const rumors = [
        'Говорят, в соседнем подземелье спрятан лабораторный ключ от зачёта.',
        'Дракон на севере принимает рефераты только в формате JSON.',
        'Стражи ворот пускают лишь тех, кто знает пароль от БД.',
        'На третьем уровне подземелья нашли нефункциональный, но красивый UI.',
        'Алхимик уверяет, что BCrypt — лучший шифр против гоблинов-хакеров.',
        'Барды спорят, что лучше: массив или связный список для табуляции.'
    ];

    const questStarts = ['Староста деревни просит', 'Неизвестный маг умоляет', 'Гильдия следопытов поручает', 'Дракон в отпуске хочет'];
    const questGoals = ['вернуть похищенный амулет', 'очистить заброшенную лабораторию', 'доставить свиток без лишних глаз', 'научить студентов пользоваться графиком'];
    const questTwists = ['но путь охраняют баги старого кода', 'и времени всего до рассвета', 'при этом за вами следят конкуренты', 'но награда — безлимитный кофе'];

    const npcNames = ['Лира Пиксель', 'Торгрим Скриптолов', 'Майя Табулия', 'Сэр Рефактор', 'Далия JSON', 'Бром Оптимус'];
    const npcTraits = ['боится темноты, но любит драконов', 'говорит только двоичным кодом', 'коллекционирует магические массивы', 'никогда не расстаётся с плащом-невидимкой', 'мечтает стать full-stack бардом', 'всегда спорит о стиле кода'];

    const monsterTable = [
        { name: 'Гоблин-тестер', hp: 14, dmg: [2, 6] },
        { name: 'Скелет-рефактор', hp: 18, dmg: [3, 7] },
        { name: 'Драконья тень', hp: 22, dmg: [4, 8] },
    ];

    const raceTracks = ['Крыса-маг', 'Боевая мышь', 'Тень канализации', 'Дворфийский хомяк'];
    const eventDeck = [
        'Заказчик просит срочно доставить посылку через запретный лес.',
        'На площади идёт турнир арбалетчиков — ставки принимаются.',
        'В трактир заходит инспектор гильдии — все замирают.',
        'Ваша партия получает приглашение на бал маскарад.',
        'Портал внезапно открывается в лабораторию некроманта.',
    ];
    const riddles = [
        { q: 'Что можно сломать, не касаясь его?', a: 'тишину' },
        { q: 'Утром на четырёх, днём на двух, вечером на трёх — что это?', a: 'человек' },
        { q: 'Что становится мокрее, когда сушит?', a: 'полотенце' },
        { q: 'Что принадлежит вам, но другие используют чаще?', a: 'имя' },
        { q: 'Без чего не обойтись в бою с драконом, но это нельзя купить?', a: 'смелость' },
        { q: 'Что можно увидеть с закрытыми глазами?', a: 'сон' },
        { q: 'Кто ходит ночью без ног и днём без тени?', a: 'ветер' },
        { q: 'Что растёт вниз головой?', a: 'сосулька' },
        { q: 'Летит без крыльев, плачет без глаз?', a: 'облако' },
        { q: 'Что можно держать только открытым?', a: 'дверь' },
        { q: 'Что за зверь: хвоста нет, усы есть, а мурчать умеет сервер?', a: 'чат-бот' },
        { q: 'Что может загореться, хотя не из дерева и не из масла?', a: 'идея' },
        { q: 'Чем больше из неё берёшь, тем больше она становится?', a: 'яма' },
        { q: 'Что не имеет корней, но растёт, не имеет глаз, но плачет?', a: 'облако' },
        { q: 'На что можно смотреть часами и не увидеть движения, но оно проходит?', a: 'тень' },
        { q: 'Что бегает по домам, но ног нет?', a: 'пыль' },
        { q: 'Что живёт в кубике, но показывает круги судьбы?', a: 'двадцатка'},
        { q: 'свободное действие', a: 'плакать', unlock: true},
        { q: 'Что держит историю партии, но не слышит песен барда?', a: 'журнал' },
        { q: 'Кто приходит в подземелье без брони, но держит всех в тонусе?', a: 'мастер' }
    ];

    const bunkerRoles = ['Полевой врач', 'Инженер-механик', 'Социолог', 'Плотник', 'Пожарный', 'Лингвист', 'Повар-полевик', 'Пилот планера', 'Фермер-гидропонист', 'Учёный-биолог', 'Программист', 'Стример', 'Философ', 'Поэт', 'Электрик', 'Сантехник', 'Маг-колдун в 3м поколении'];
    const bunkerTraits = ['боится темноты', 'ведёт дневник', 'умеет чинить всё из подручных средств', 'танцует под любую музыку', 'знает наизусть все стихи Пушкина', 'читает мысли кошек', 'умеет шить', 'юморист', 'знает 100 рецептов из консервов', 'прошёл курс первой помощи', 'вспыльчивый', 'терпеливый', 'превосходный лидер', 'не трогал траву веками'];
    const bunkerItems = ['радиостанция с ручным приводом', 'ящик консервов', 'фильтр для воды', 'портативная солнечная панель', 'набор семян', 'дрон-разведчик', 'аптечка', 'полевой набор инструментов', '3D-принтер для деталей', 'термос на 10 литров', 'Корова', 'амбар хлеба', 'дакимакура с пуджом', 'рюкзак консерв', 'кот', 'лимитированная лабуба', 'книги по микробиологии', 'игральные карты', 'карты уно'];
    const bunkerCatastrophes = ['ядерная зима', 'метеоритный дождь', 'зомби-эпидемия', 'солнечная вспышка, сжигающая электронику', 'тотальный потоп', 'наноботы вышли из-под контроля', '7я лаба'];
    const bunkerRooms = ['бункер с гидропоникой(еды на 2 года)', 'бункер с радиолабораторией(еды хватит всегда)', 'склад с генераторами(еды почти нет)', 'убежище с библиотекой(еды на 6 месяцев)', 'подземный гараж(еды на 4 месяца)', 'бункер с теплицей(еда восполняема но может быть нехватка воды)'];
    const bunkerNames = ['Анна', 'Борис', 'Вика', 'Григорий', 'Даша', 'Егор', 'Женя', 'Зоя', 'Илья', 'Кира', 'Лев', 'Мила', 'Никита', 'Олеся', 'Павел', 'Нилл', 'Шерлок', 'Слава'];
    const bunkerHobbies = ['рисование карт', 'кулинария', 'гонки', 'скалолазание', 'апноэ', 'настольные ролевки', 'садоводство', 'музыка на укулеле'];
    const bunkerConditions = ['аллергия на пыль', 'прекрасное здоровье', 'перелом плеча', 'повышенная выносливость', 'нервное напряжение', 'стальная психика', 'астма', 'медицинский имплант-кардиостимулятор', 'Рак мозга критическая степень', 'Рак лёгких средняя степень', 'лихорадка', 'неизвестная болезнь', 'грипп', 'сталинский самовар', 'паралич', 'шизофрения'];
    const bunkerSpecials = [
        {
            title: 'Медицинская поддержка',
            summary: 'снимает тяжёлое состояние с одного союзника',
            effect: (actor, state) => {
                const target = state.survivors.find(s => /перелом|астма|напряж|аллерг/i.test(s.condition));
                if (target) {
                    target.condition = 'стабилизирован, под наблюдением медика';
                    return `${actor.name} стабилизирует ${target.name}.`;
                }
                return `${actor.name} не нашёл пациентов — зато все спокойны.`;
            },
        },
        {
            title: 'Инженерный апгрейд',
            summary: 'находит импровизированный модуль для укрытия',
            effect: (actor, state) => {
                const target = state.survivors[randInt(0, state.survivors.length - 1)];
                const bonus = bunkerItems[randInt(0, bunkerItems.length - 1)];
                target.item = `${target.item} + ${bonus}`;
                return `${actor.name} усиливает снаряжение ${target.name}: ${bonus}.`;
            },
        },
        {
            title: 'Харизматичный лидер',
            summary: 'раскрывает скрытые факты и возвращает надежду',
            effect: (actor, state) => {
                let revealed = 0;
                state.survivors.forEach(s => {
                    const order = ['role', 'trait', 'item', 'hobby', 'condition', 'special'];
                    const hidden = order.find(key => !s.revealed.includes(key));
                    if (hidden) {
                        s.revealed.push(hidden);
                        revealed += 1;
                    }
                });
                return revealed ? `${actor.name} вдохновляет группу и открывает ${revealed} факт(ов).` : `${actor.name} напоминает всем держаться вместе.`;
            },
        },
        {
            title: 'Разведка',
            summary: 'привозит новые припасы и открывает предмет',
            effect: (actor, state) => {
                const target = state.survivors[randInt(0, state.survivors.length - 1)];
                const cache = ['запас фильтров', 'комплект батарей', 'склад масок', 'пакет консервов'];
                target.item = `${target.item} + ${cache[randInt(0, cache.length - 1)]}`;
                if (!target.revealed.includes('item')) target.revealed.push('item');
                return `${actor.name} возвращается с разведки для ${target.name}.`;
            },
        },
    ];
    const bunkerEvents = ['вентиляция ломается — нужна помощь инженера', 'рация ловит выживших поблизости', 'вода кончается, надо выйти за припасами', 'кто-то теряет сознание — требуется медик', 'психологическая ссора грозит взорвать коллектив', 'наружу прорвалась радиация — нужен фильтр'];
    const bunkerState = { catastrophe: '', room: '', survivors: [], round: 0 };

    function burstPortal(value) {
        if (!portal || !portalValue) return;
        portalValue.textContent = value;
        portalValue.classList.remove('show');
        portal.classList.remove('crit', 'fumble');
        void portalValue.offsetWidth;
        portalValue.classList.add('show');

        if (value === 20) {
            portal.classList.add('crit');
            spawnShockwave('crit');
            spawnSigils('crit');
        } else if (value === 1) {
            portal.classList.add('fumble');
            spawnShockwave('fumble');
            spawnSigils('fumble');
        }

        const bursts = (value === 20 ? 14 : value === 1 ? 10 : 9) + Math.floor(Math.random() * 5);
        for (let i = 0; i < bursts; i++) {
            const spark = document.createElement('span');
            spark.className = 'burst';
            if (value === 20) spark.classList.add('burst-crit');
            if (value === 1) spark.classList.add('burst-fumble');

            const angle = Math.random() * Math.PI * 2;
            const radius = 42 + Math.random() * 55;
            const x = 50 + Math.cos(angle) * radius;
            const y = 50 + Math.sin(angle) * radius;
            spark.style.left = `${x}%`;
            spark.style.top = `${y}%`;
            spark.style.animationDuration = `${620 + Math.random() * 520}ms`;
            portal.appendChild(spark);
            setTimeout(() => spark.remove(), 1200);
        }

        if (diceReadout) diceReadout.textContent = `Выпало: ${value}`;
    }

    function spawnShockwave(kind) {
        const ring = document.createElement('span');
        ring.className = `shockwave ${kind || ''}`.trim();
        portal.appendChild(ring);
        setTimeout(() => ring.remove(), 1400);
    }

    function spawnSigils(kind) {
        for (let i = 0; i < 6; i++) {
            const shard = document.createElement('span');
            shard.className = `sigil ${kind || ''}`.trim();
            const angle = Math.random() * Math.PI * 2;
            const distance = 20 + Math.random() * 35;
            shard.style.setProperty('--tx', `${Math.cos(angle) * distance}%`);
            shard.style.setProperty('--ty', `${Math.sin(angle) * distance}%`);
            shard.style.animationDelay = `${i * 40}ms`;
            portal.appendChild(shard);
            setTimeout(() => shard.remove(), 1200);
        }
    }

    function roll() {
        const value = Math.floor(Math.random() * 20) + 1;
        lastRoll = value;
        burstPortal(value);
        const text = value === 20 ? 'Критический успех! 🐲' : value === 1 ? 'Провал... 🐉' : `Выпало ${value}`;
        log.textContent = text;
        return value;
    }

    function brewPotion() {
        if (!miniGameBoard) return;
        const reagents = ['лепестки лунной розы', 'пепел феникса', 'слёзы саламандры', 'стекло разбитой фляги'];
        const effects = ['+2 к ловкости', 'зрение в темноте', 'устойчивость к огню', 'способность говорить с крысами'];
        miniGameBoard.textContent = `Смешали ${reagents[randInt(0, reagents.length - 1)]} и ${reagents[randInt(0, reagents.length - 1)]} → эффект: ${effects[randInt(0, effects.length - 1)]}.`;
    }

    function logBunker(text) {
        if (!bunkerLog) return;
        const line = document.createElement('div');
        line.textContent = text;
        bunkerLog.appendChild(line);
        bunkerLog.scrollTop = bunkerLog.scrollHeight;
    }

    function describeFact(key, survivor) {
        const map = {
            role: `профессия: ${survivor.role}`,
            trait: `характеристика: ${survivor.trait}`,
            item: `предмет: ${survivor.item}`,
            hobby: `хобби: ${survivor.hobby}`,
            condition: `здоровье: ${survivor.condition}`,
            special: `способность: ${survivor.special.title} (${survivor.special.summary})`,
        };
        return map[key];
    }

    function createSurvivor() {
        const hasCrypto = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function';
        return {
            id: `b-${hasCrypto ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(16).slice(2)}`}`,
            name: bunkerNames[randInt(0, bunkerNames.length - 1)],
            age: randInt(18, 60),
            role: bunkerRoles[randInt(0, bunkerRoles.length - 1)],
            trait: bunkerTraits[randInt(0, bunkerTraits.length - 1)],
            item: bunkerItems[randInt(0, bunkerItems.length - 1)],
            hobby: bunkerHobbies[randInt(0, bunkerHobbies.length - 1)],
            condition: bunkerConditions[randInt(0, bunkerConditions.length - 1)],
            special: bunkerSpecials[randInt(0, bunkerSpecials.length - 1)],
            revealed: [],
            status: 'queue',
        };
    }

    function renderBunker() {
        if (!bunkerBoard) return;
        if (bunkerSelect) bunkerSelect.innerHTML = '';
        if (!bunkerState.survivors.length) {
            bunkerBoard.textContent = 'Нажмите «Новая партия», чтобы создать катастрофу и выживших.';
            return;
        }

        const { catastrophe, room, round, survivors } = bunkerState;
        const lines = survivors.map((s, idx) => {
            const status = s.status === 'safe' ? 'в бункере' : s.status === 'evicted' ? 'изгнан' : 'ожидает';
            const facts = s.revealed.length ? s.revealed.map(key => describeFact(key, s)).join('; ') : 'карта закрыта';
            const badge = s.status === 'safe' ? '🛡️' : s.status === 'evicted' ? '🚪' : '⏳';
            if (bunkerSelect) {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = `${idx + 1}. ${s.name} (${status})`;
                bunkerSelect.appendChild(opt);
            }
            return `${badge} ${idx + 1}) ${s.name}, ${s.age} — ${facts}`;
        }).join('\n');

        bunkerBoard.textContent = `Катастрофа: ${catastrophe}. Укрытие: ${room}. Раунд ${round}\n${lines}`;
    }

    function selectSurvivor() {
        if (!bunkerState.survivors.length) return null;
        const pickId = bunkerSelect?.value || bunkerState.survivors[0].id;
        return bunkerState.survivors.find(s => s.id === pickId) || bunkerState.survivors[0];
    }

    function bunkerDeal() {
        bunkerState.catastrophe = bunkerCatastrophes[randInt(0, bunkerCatastrophes.length - 1)];
        bunkerState.room = bunkerRooms[randInt(0, bunkerRooms.length - 1)];
        bunkerState.survivors = Array.from({ length: 6 }, () => createSurvivor());
        bunkerState.round = 1;
        if (bunkerLog) bunkerLog.innerHTML = '';
        if (bunkerFactSelect) bunkerFactSelect.value = 'next';
        logBunker(`Катастрофа: ${bunkerState.catastrophe}. Бункер: ${bunkerState.room}.`);
        logBunker('Выберите персонажа и открывайте факты, затем решайте судьбу каждого.');
        renderBunker();
    }

    function revealSurvivorFact() {
        const survivor = selectSurvivor();
        if (!survivor) return;
        const order = ['role', 'trait', 'item', 'hobby', 'condition', 'special'];
        const mode = bunkerFactSelect?.value || 'next';
        let next;
        if (mode === 'random') {
            const hidden = order.filter(key => !survivor.revealed.includes(key));
            if (!hidden.length) {
                logBunker(`${survivor.name}: всё уже известно.`);
                return;
            }
            next = hidden[randInt(0, hidden.length - 1)];
        } else if (mode === 'next') {
            next = order.find(key => !survivor.revealed.includes(key));
        } else {
            next = mode;
        }

        if (!next) {
            logBunker(`${survivor.name}: все факты уже раскрыты.`);
            return;
        }
        if (survivor.revealed.includes(next)) {
            logBunker(`${survivor.name}: ${describeFact(next, survivor)} уже открыт.`);
            return;
        }
        survivor.revealed.push(next);
        logBunker(`${survivor.name}: ${describeFact(next, survivor)}.`);
        renderBunker();
    }

    function markSurvivor(status) {
        const survivor = selectSurvivor();
        if (!survivor) return;
        survivor.status = status;
        logBunker(`${survivor.name} теперь ${status === 'safe' ? 'в бункере' : 'изгнан(а)'}!`);
        renderBunker();
    }

    function useSpecial() {
        const survivor = selectSurvivor();
        if (!survivor) return;
        const ability = survivor.special;
        if (!ability) {
            logBunker(`${survivor.name}: особая способность недоступна.`);
            return;
        }
        const detail = ability.effect ? ability.effect(survivor, bunkerState) : ability.summary;
        if (!survivor.revealed.includes('special')) survivor.revealed.push('special');
        logBunker(`${survivor.name} использует «${ability.title}»: ${detail}`);
        renderBunker();
    }

    function bunkerVote() {
        markSurvivor('evicted');
    }

    function bunkerSave() {
        markSurvivor('safe');
    }

    function bunkerEvent() {
        if (!bunkerState.survivors.length) return;
        bunkerState.round += 1;
        const event = bunkerEvents[randInt(0, bunkerEvents.length - 1)];
        const target = bunkerState.survivors[randInt(0, bunkerState.survivors.length - 1)];
        const needsHero = ['инженер', 'врач', 'медик'].some(word => event.toLowerCase().includes(word));
        const bonus = needsHero && target.role.toLowerCase().includes('врач') ? ' — спасает врач!' : '';
        logBunker(`Раунд ${bunkerState.round}: ${event}${bonus}`);
        const order = ['role', 'trait', 'item', 'hobby', 'condition'];
        const reveal = order.find(key => !target.revealed.includes(key));
        if (reveal) {
            target.revealed.push(reveal);
            logBunker(`${target.name} раскрывает: ${describeFact(reveal, target)}.`);
        }
        renderBunker();
    }

    function bunkerReset() {
        bunkerState.catastrophe = '';
        bunkerState.room = '';
        bunkerState.survivors = [];
        bunkerState.round = 0;
        if (bunkerLog) bunkerLog.innerHTML = '';
        renderBunker();
    }

    function randInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    function setButtons(active) {
        [attackBtn, defendBtn, restBtn, spellBtn].forEach(btn => {
            if (!btn) return;
            btn.disabled = !active;
        });
    }

    function updateStats() {
        if (!gameStats) return;
        if (!gameState.hero) {
            gameStats.textContent = 'Нажмите «Начать», чтобы войти в подземелье.';
            return;
        }
        gameStats.textContent = `Вы: ${gameState.hero.hp} HP / ${gameState.hero.stamina} STA / ${gameState.sparks} СПЛ · Противник: ${gameState.foe.name} (${gameState.foe.hp} HP)`;
    }

    function logLine(text) {
        if (!gameLog) return;
        const time = new Date().toLocaleTimeString();
        const div = document.createElement('div');
        div.textContent = `[${time}] ${text}`;
        gameLog.appendChild(div);
        gameLog.scrollTop = gameLog.scrollHeight;
    }

    function startAdventure() {
        gameState.hero = { hp: 28, stamina: 3 };
        gameState.foe = { ...monsterTable[randInt(0, monsterTable.length - 1)] };
        gameState.guard = false;
        gameState.sparks = 1;
        if (gameLog) gameLog.innerHTML = '';
        logLine(`В подземелье вас встречает ${gameState.foe.name}.`);
        setButtons(true);
        updateStats();
    }

    function checkEnd() {
        if (gameState.foe.hp <= 0) {
            logLine('Победа! Сокровища ваши.');
            setButtons(false);
            return true;
        }
        if (gameState.hero.hp <= 0) {
            logLine('Вы пали в бою. Попробуйте ещё раз.');
            setButtons(false);
            return true;
        }
        return false;
    }

    function foeStrike() {
        const dmg = randInt(gameState.foe.dmg[0], gameState.foe.dmg[1]);
        const mitigated = gameState.guard ? Math.max(1, Math.floor(dmg / 2)) : dmg;
        gameState.hero.hp -= mitigated;
        logLine(`${gameState.foe.name} бьёт на ${mitigated} урона${gameState.guard ? ' (смягчено защитой)' : ''}.`);
        gameState.guard = false;
    }

    function attack() {
        if (!gameState.hero) return;
        const rollVal = randInt(1, 20);
        const bonus = rollVal >= 18 ? 8 : rollVal <= 4 ? 2 : 5;
        const dmg = bonus + randInt(0, 4);
        gameState.foe.hp -= dmg;
        logLine(`Вы атакуете (d20=${rollVal}) и наносите ${dmg} урона.`);
        if (checkEnd()) { updateStats(); return; }
        foeStrike();
        checkEnd();
        updateStats();
    }

    function castSpell() {
        if (!gameState.hero) return;
        if (gameState.sparks <= 0) {
            logLine('Магическая энергия иссякла.');
            return;
        }
        gameState.sparks -= 1;
        const rollVal = roll();
        const dmg = 6 + Math.floor(rollVal / 2);
        gameState.foe.hp -= dmg;
        logLine(`Вы чертите руны (d20=${rollVal}) и прожигаете врага на ${dmg} урона.`);
        if (checkEnd()) { updateStats(); return; }
        if (Math.random() < 0.4) {
            const recoil = randInt(1, 4);
            gameState.hero.hp -= recoil;
            logLine(`Отдача магии бьёт по вам (${recoil} урона).`);
        }
        foeStrike();
        checkEnd();
        updateStats();
    }

    function defend() {
        if (!gameState.hero) return;
        gameState.guard = true;
        logLine('Вы поднимаете щит: следующий удар наносит половину урона.');
        foeStrike();
        checkEnd();
        updateStats();
    }

    function rest() {
        if (!gameState.hero || gameState.hero.stamina <= 0) {
            logLine('Вы слишком вымотаны, чтобы отдыхать.');
            return;
        }
        const heal = randInt(4, 8);
        gameState.hero.hp = Math.min(gameState.hero.hp + heal, 28);
        gameState.hero.stamina -= 1;
        logLine(`Вы делаете глоток эля и восстанавливаете ${heal} HP (осталось выносливости: ${gameState.hero.stamina}).`);
        foeStrike();
        checkEnd();
        updateStats();
    }

    function resolveSkill() {
        if (!skillResult || !skillSelect || !skillDc) return;
        const dc = parseInt(skillDc.value, 10) || 10;
        const bonus = Math.floor(Math.random() * 6) + 1;
        const rollVal = roll();
        const total = rollVal + bonus;
        const success = total >= dc;
        skillResult.textContent = `${skillSelect.value}: d20=${rollVal} + бонус ${bonus} = ${total} → ${success ? 'успех' : 'провал'} (КС ${dc})`;
    }

    function resolveArena(action) {
        if (!arenaLog) return;
        const rollVal = roll();
        const swing = Math.floor(Math.random() * 4) - 1;
        const scores = { feint: 2, strike: 3, bribe: 1 };
        const total = rollVal + (scores[action] || 0) + swing;
        const phrases = {
            feint: 'Вы отвлекаете гоблина блестящей монеткой',
            strike: 'Вы атаковали с размаху',
            bribe: 'Гоблин щурится на протянутый кошель'
        };
        const outcome = total >= 15 ? 'Победа! Гоблин сдаётся.'
            : total >= 10 ? 'Ничья: гоблин сбегает, но вы не ранены.'
            : 'Поражение: получите по ушам и попытайтесь снова.';
        arenaLog.textContent = `${phrases[action] || 'Хитрый манёвр'} (d20=${rollVal}) → ${outcome}`;
    }

    function spinWheel() {
        if (!wheelResult) return;
        const effects = [
            'Благословение: следующий бросок +2',
            'Скользкий пол: первый удар по вам промахивается',
            'Мешочек золота: получите редкий лут',
            'Старый бард: рассказывает подсказку к любому квесту',
            'Туман: враги бьют вслепую (-2 к их атакам)',
            'Вдохновение: переброс одного d20'
        ];
        const choice = effects[Math.floor(Math.random() * effects.length)];
        wheelResult.textContent = `Колесо замедляется… ${choice}`;
    }

    function runRace() {
        if (!miniGameBoard) return;
        const lane = raceTracks[Math.floor(Math.random() * raceTracks.length)];
        const rolls = [roll(), roll(), roll()];
        const score = rolls.reduce((a, b) => a + b, 0);
        miniGameBoard.textContent = `Ваша ${lane} пробежала дистанцию с бросками ${rolls.join(', ')} → итог ${score}. ${score >= 30 ? 'Триумфальная победа!' : 'Ещё тренироваться.'}`;
    }

    function drawCard() {
        if (!miniGameBoard) return;
        const card = eventDeck[Math.floor(Math.random() * eventDeck.length)];
        miniGameBoard.textContent = `Карта приключения: ${card}`;
    }

    function askRiddle() {
        if (!miniGameBoard) return;
        const pick = riddles[Math.floor(Math.random() * riddles.length)];
        const answer = prompt(`${pick.q}\n(подсказка: одно слово)`) || '';
        const ok = answer.trim().toLowerCase() === pick.a;
        miniGameBoard.textContent = ok ? 'Верно! Барды хлопают.' : `Ответ: ${pick.a}. Попробуйте ещё.`;

        if (ok && pick.unlock && lastRoll === 20 && !secretUnlocked) {
            secretUnlocked = true;
            const win = window.open('https://disk.yandex.ru/client/disk/пасхалка', '_blank', 'noopener');
            if (!win) {
                miniGameBoard.textContent += ' (Разрешите всплывающее окно, чтобы увидеть скрытую ссылку!)';
            }
        }
    }

    document.getElementById('rollD20').addEventListener('click', roll);
    rerollBtn?.addEventListener('click', roll);
    skillBtn?.addEventListener('click', resolveSkill);
    arenaButtons.forEach(btn => btn.addEventListener('click', () => resolveArena(btn.dataset.arena)));
    wheelSpin?.addEventListener('click', spinWheel);
    raceBtn?.addEventListener('click', runRace);
    cardBtn?.addEventListener('click', drawCard);
    riddleBtn?.addEventListener('click', askRiddle);
    brewBtn?.addEventListener('click', brewPotion);
    bunkerDealBtn?.addEventListener('click', bunkerDeal);
    bunkerVoteBtn?.addEventListener('click', bunkerVote);
    bunkerRevealBtn?.addEventListener('click', revealSurvivorFact);
    bunkerSpecialBtn?.addEventListener('click', useSpecial);
    bunkerSaveBtn?.addEventListener('click', bunkerSave);
    bunkerEventBtn?.addEventListener('click', bunkerEvent);
    bunkerResetBtn?.addEventListener('click', bunkerReset);

    renderBunker();

    if (lootBtn) lootBtn.addEventListener('click', () => {
        lootList.innerHTML = '';
        const count = 3 + Math.floor(Math.random() * 3);
        for (let i = 0; i < count; i++) {
            const item = lootTable[Math.floor(Math.random() * lootTable.length)];
            const li = document.createElement('li');
            li.textContent = `• ${item}`;
            lootList.appendChild(li);
        }
    });

    if (rumorBtn) rumorBtn.addEventListener('click', () => {
        const text = rumors[Math.floor(Math.random() * rumors.length)];
        rumorText.textContent = text;
    });

    if (questBtn) questBtn.addEventListener('click', () => {
        const quest = `${questStarts[Math.floor(Math.random() * questStarts.length)]} ${questGoals[Math.floor(Math.random() * questGoals.length)]}, ${questTwists[Math.floor(Math.random() * questTwists.length)]}.`;
        questText.textContent = quest;
    });

    if (npcBtn) npcBtn.addEventListener('click', () => {
        const npc = `${npcNames[Math.floor(Math.random() * npcNames.length)]} — ${npcTraits[Math.floor(Math.random() * npcTraits.length)]}.`;
        npcText.textContent = npc;
    });

    if (startBtn) startBtn.addEventListener('click', startAdventure);
    if (attackBtn) attackBtn.addEventListener('click', attack);
    if (defendBtn) defendBtn.addEventListener('click', defend);
    if (spellBtn) spellBtn.addEventListener('click', castSpell);
    if (restBtn) restBtn.addEventListener('click', rest);

    burstPortal(20);
    roll();
})();

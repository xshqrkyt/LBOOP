(function(){
    function assertEqual(actual, expected, message){
        if(actual !== expected){
            throw new Error(`${message}: ожидалось ${expected}, получили ${actual}`);
        }
    }

    function assertClose(actual, expected, eps, message){
        if(Math.abs(actual - expected) > eps){
            throw new Error(`${message}: ожидалось ${expected}±${eps}, получили ${actual}`);
        }
    }

    function run(){
        const api = window.AppAPI;
        api.state.functions.length = 0;
        const a = api.pushFunction('a', [{x:0,y:0},{x:1,y:1}]);
        const b = api.pushFunction('b', [{x:0,y:1},{x:1,y:3}]);
        api.binaryOp('add');
        const sum = api.state.functions.find(f => f.name.includes('add'));
        assertEqual(sum.points[1].y, 4, 'Сложение по второй точке');

        document.getElementById('chartSource').value = a.id;
        const applied = api.applyValue();
        assertClose(applied, 0, 1e-6, 'apply выбирает ближайшую точку');

        document.getElementById('integralSource').value = b.id;
        document.getElementById('integralThreads').value = 2;
        const area = api.integrate();
        assertClose(area, 2, 1e-6, 'Трапецоидальное интегрирование');

        const cloud = Array.from({length: 100000}, (_, i) => ({ x: i, y: Math.sin(i/50)}));
        const sampled = api.downsampleForCanvas(cloud, 800);
        if (sampled.length > 2000) throw new Error('Даунсэмплинг не ограничил размер');
        if (sampled[0].x !== 0 || sampled[sampled.length-1].x !== 99999) throw new Error('Экстремумы потеряны при сэмплинге');

        const ticks = api.niceTicks(-0.12, 3.9);
        if (!ticks.includes(0)) throw new Error('Тики должны включать 0 при пересечении оси');
        if (api.formatTick(123456) !== '1.2e+5') throw new Error('Форматирование больших чисел сломано');

        return 'Все проверки прошли';
    }

    window.runTests = run;
})();

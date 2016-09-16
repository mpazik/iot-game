require.config({
    paths: {
        "configuration": "../dev/configuration",
        "extra-ui": "../dev/extra-ui",
        "pixi": "render/pixi"
    }
});

define([], () => {
    function isArrowFunctionSupported() {
        try {
            eval('()=>{}');
            return true;
        } catch (e) {
            return false;
        }
    }

    function isCustomElementsSupported() {
        return document.registerElement && typeof document.registerElement === 'function';
    }

    if (!(isArrowFunctionSupported() && isCustomElementsSupported())) {
        document.body.innerHTML = '<div id="not-supported-browser" style="' +
            'background: rgba(0, 0, 0, 0.7); border: #777 solid 1px; border-radius: 5px;' +
            'width: 400px; margin: 120px auto 0 auto; padding: 20px;' +
            'line-height: 30px; font-size: 20px; color: #EEE;">' +
            '<p>Only the latest version of <a href="https://www.google.com/chrome">chrome browser</a> is currently supported.</p>' +
            '<p>Sorry for inconvenience.</p></div>';
        return;
    }

    require(['ui/game', 'ui/game-ui'], function () {
        const game = document.createElement("dzida-game");
        document.body.appendChild(game);

        const ui = document.createElement("game-ui");
        ui.addEventListener('element-attached', function () {
            ui.init();
        });
        document.body.appendChild(ui);
    });
});


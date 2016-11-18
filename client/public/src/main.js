require.config({
    paths: {
        "configuration": "../dev/configuration",
        "extra-ui": "../dev/extra-ui",
        "pixi": "render/pixi"
    }
});

define([], () => {
    function isChromeBrowser() {
        return !!window.chrome && !!window.chrome.webstore;
    }

    if (!isChromeBrowser()) {
        document.body.innerHTML = '<div id="not-supported-browser" style="' +
            'background: rgba(0, 0, 0, 0.7); border: #777 solid 1px; border-radius: 5px;' +
            'width: 400px; margin: 120px auto 0 auto; padding: 20px;' +
            'line-height: 30px; font-size: 20px; color: #EEE;">' +
            '<p>Only the latest version of <a href="https://www.google.com/chrome">chrome browser</a> is currently supported.</p>' +
            '<p>Sorry for inconvenience.</p></div>';
        return;
    }

    require(['ui/game', 'ui/game-ui'], function (game, gameUi) {
        document.body.appendChild(game.create());
        game.start();

        const uiElement = document.createElement('div');
        uiElement.setAttribute('id', 'game-ui');
        document.body.appendChild(uiElement);
        gameUi.init(uiElement);
    });
});


require.config({
    paths: {
        "lib/react": "../components/react/react-with-addons",
        "JSXTransformer": "../components/react/JSXTransformer",
        "jsx": "../components/requirejs-react-jsx/jsx",
        "text": "../components/requirejs-text/text",
        "lib/pixi": "../components/pixi.js/bin/pixi"
    },

    shim: {
        "react": {
            "exports": "React"
        },
        "JSXTransformer": "JSXTransformer"
    },

    config: {
        jsx: {
            fileExtension: ".js",
            transformOptions: {
                harmony: false,
                stripTypes: false,
                inlineSourceMap: false
            },
            usePragma: false
        }
    }
});

function deffer(func) {
    setTimeout(func, 0);
}

require(['jsx!react/app', './polyfill'], function (App) {
    var app = new App();
    app.init()
});
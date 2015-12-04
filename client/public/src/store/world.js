define(function (require, exports, module) {
    const StoreRegistrar = require('../component/store-registrar');

    const key = 'world';
    var state = {
        name: null,
        width: null,
        height: null,
        tileset: null,
        spawnPoint: null,
        tiles: null
    };

    const init = (initialState) => state = initialState;
    StoreRegistrar.registerStore({
        key,
        eventHandler: null,
        state: () => state,
        init
    });


    module.exports = {
        key,
        init,
        state: () => state,
        tileSize: () => state.tileset.tileSize,
        spawnPoint: () => state.spawnPoint
    };
});
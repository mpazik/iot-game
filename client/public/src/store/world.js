define(function (require, exports, module) {
    const StoreRegistrar = require('../component/store-registrar');
    const Resources = require('./resources');

    const key = 'world';
    var state = {
        name: null,
        width: null,
        height: null,
        tileset: null,
        spawnPoint: null,
        tiles: null
    };
    const tileset = () => Resources.tileset(state.tileset);

    const init = (initialState) => state = initialState;

    StoreRegistrar.registerStore({
        key,
        eventHandler: null,
        state: () => state,
        init
    });
    const tile = (tx, ty) => state.tiles[ty * state.width + tx];
    const terrain = (tx, ty) => tileset().terrains[tile(tx, ty)];

    module.exports = {
        key,
        init,
        state: () => state,
        tileSize: () => state.tileset.tileSize,
        spawnPoint: () => state.spawnPoint,
        tile, terrain
    };
});
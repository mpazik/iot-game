define(function (require, exports, module) {
    const StoreRegistrar = require('../component/store-registrar');
    const Resources = require('./resources');

    const key = 'worldObject';
    const state = new Map();

    const eventHandlers = {
    };

    StoreRegistrar.registerStore({
        key,
        eventHandlers,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(worldObject => {
                state.set(worldObject.id, worldObject.data)
            });
        }
    });

    module.exports = {
        key,
        objects: () => Array.from(state.values()),
        kindDefinition: (kind) => Resources.objectKind(kind)
    };
});
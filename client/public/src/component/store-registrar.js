define(function (require, exports, module) {
    const ResponseIds = require('../common/packet/messages').ids;
    const PacketDispatcher = require('./dispatcher');

    const stores = new Map();

    //client specific
    PacketDispatcher.messageStream.subscribe(ResponseIds.InitialData, (initialData) => {
        stores.forEach((store, key) => store.init(initialData.state[key]))
    });

    module.exports = {
        //server specific
        storeStates: () => Map.toObject(stores.map((store) => store.state())),
        //server specific
        initStore: (key, state) => {
            stores.get(key).init(state);
        },
        registerStore: (store) => {
            stores.set(store.key, store);
            if (store.eventHandlers) {
                Object.forEach(store.eventHandlers, (handler, id) => {
                    PacketDispatcher.messageStream.subscribe(id, handler);
                })
            }
        }
    }
});
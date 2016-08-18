define(function (require, exports, module) {
    const Messages = require('./instnace/messages');
    const Dispatcher = require('./dispatcher');

    const stores = new Map();

    //client specific
    Dispatcher.messageStream.subscribe(Messages.InitialData, (initialData) => {
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
        }
    }
});
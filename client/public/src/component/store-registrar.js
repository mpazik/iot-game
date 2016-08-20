define(function (require, exports, module) {
    const Messages = require('./instance/messages');
    const Dispatcher = require('./dispatcher');

    const stores = new Map();

    //client specific
    Dispatcher.messageStream.subscribe(Messages.InitialData, (initialData) => {
        stores.forEach((store, key) => store.init(initialData.state[key]))
    });

    module.exports = {
        registerStore: (store) => {
            stores.set(store.key, store);
        }
    }
});
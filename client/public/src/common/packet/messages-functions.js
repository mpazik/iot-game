define(function (require, exports, module) {
    module.exports = {
        setIdToPrototype: function (messages) {
            Object.keys(messages.constructors).forEach(function (key) {
                messages.constructors[key].prototype.messageId = messages.ids[key];
            });
        },
        createForIds: function (messages) {
            Object.keys(messages.ids).forEach(function (key) {
                const id = messages.ids[key];
                messages.forId[id] = messages.constructors[key];
            });
        }
    };
});
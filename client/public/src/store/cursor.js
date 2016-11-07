define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');

    var updateCursor = null;

    module.exports = {
        setCursor(name) {
            updateCursor(name)
        },
        setDefault() {
            updateCursor(null)
        },
        cursorType: new Publisher.StatePublisher(null, (push) => updateCursor = push)
    };
});

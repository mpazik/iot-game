define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');

    var notify = null;

    module.exports = {
        notify(text) {
            notify(text)
        },
        publisher: new Publisher.StreamPublisher((push) => notify = push)
    };
});

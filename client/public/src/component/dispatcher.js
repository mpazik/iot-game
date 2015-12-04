define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');

    module.exports = {
        messageStream: new Publisher.OpenPublisher(),
        userEventStream: new Publisher.OpenPublisher(),
        keyPressStream: new Publisher.OpenPublisher()
    };
});
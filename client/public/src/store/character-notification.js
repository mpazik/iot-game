define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');

    var notify = null;
    var nextNotifyTime = 0;

    const notificationTimeSpace = 500;

    module.exports = {
        notify(text) {
            const currentTime = Date.now();
            if (nextNotifyTime < currentTime) {
                //noinspection JSUnusedAssignment
                nextNotifyTime = currentTime + notificationTimeSpace;
                notify(text);
            } else {
                setTimeout(() => notify(text), nextNotifyTime - currentTime);
                //noinspection JSUnusedAssignment
                nextNotifyTime += notificationTimeSpace
            }
        },
        publisher: new Publisher.StreamPublisher((push) => notify = push)
    };
});

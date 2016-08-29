if (typeof exports === 'object' && typeof exports.nodeName !== 'string' && typeof define !== 'function') {
    var define = function (factory) {
        factory(require, exports, module);
    };
}
define(function (require, exports, module) {

    const Ids = {
        ARROW: 0,
        STICK: 1,
        APPLE: 2
    };

    module.exports = {
        Ids
    };
});
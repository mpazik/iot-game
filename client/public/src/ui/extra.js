define(function (require, exports, module) {
    const debugWindow = require('../../dev/windows/debug-window');
    return {
        fragments: [],
        windows: [debugWindow]
    }
});
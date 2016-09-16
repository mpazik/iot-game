define((require) => {
    const debugWindow = require('../dev/windows/debug-window');
    return {
        fragments: [],
        windows: [debugWindow]
    }
});
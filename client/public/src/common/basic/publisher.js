define(function (require, exports, module) {

    function publish(event) {
        this.listeners.forEach(function (listener) {
            return listener(event);
        });
    }

    function StreamPublisher(callback) {
        this.listeners = [];
        callback(publish.bind(this))
    }

    StreamPublisher.prototype = {
        subscribe: function (listener) {
            this.listeners.push(listener);
        },
        subscribeOnce: function (listener) {
            this.subscribe(function (event) {
                listener(event);
                this.unsubscribe(listener);
            }.bind(this));
        },
        unsubscribe: function (callback) {
            var index = this.listeners.indexOf(callback);
            this.listeners.splice(index, 1);
        }
    };
    module.exports.StreamPublisher = StreamPublisher;

    function StatePublisher(value, callback) {
        this._value = value;
        this.listeners = [];
        this.stateListeners = [];
        const newPublish = function (state) {
            this._value = state;
            publish.call(this, state);
            var givenStateListeners = this.stateListeners[state];
            if (givenStateListeners && givenStateListeners.length > 0) {
                givenStateListeners.forEach(function (listener) {
                    return listener(state);
                });
            }
        }.bind(this);
        callback(newPublish);
    }

    StatePublisher.prototype = StreamPublisher.prototype;
    StatePublisher.subscribe = function (type, listener) {
        if (!this.stateListeners[type]) {
            this.stateListeners[type] = [];
        }
        this.stateListeners[type].push(listener);
    };
    StatePublisher.unsubscribe = function (type, listener) {
        if (!this.stateListeners[type]) return;
        var index = this.stateListeners[type].indexOf(listener);
        this.stateListeners[type].splice(index, 1);
    };


    Object.defineProperty(StatePublisher.prototype, "value", {
        get: function () {
            return this._value;
        }
    });
    module.exports.StatePublisher = StatePublisher;

    function DeferringPublisher(callback) {
        this.listeners = [];
        const newPublish = function (event) {
            setTimeout(function () { publish.call(this, event) }, 0);
        }.bind(this);
        callback(newPublish);
    }

    DeferringPublisher.prototype = StreamPublisher.prototype;
    module.exports.DeferringPublisher = DeferringPublisher;


    function publishType(type, event) {
        if (!this.listeners[type]) return;
        this.listeners[type].forEach(function (listener) {
            return listener(event);
        });
    }

    function TypePublisher(callback) {
        this.listeners = [];
        callback(publishType.bind(this));
    }

    TypePublisher.prototype = {
        subscribe: function (type, listener) {
            if (!this.listeners[type]) {
                this.listeners[type] = [];
            }
            this.listeners[type].push(listener);
        },
        subscribeOnce: function (type, listener) {
            this.subscribe(type, function (event) {
                listener(event);
                this.unsubscribe(type, listener);
            }.bind(this));
        },
        unsubscribe: function (type, listener) {
            if (!this.listeners[type]) return;
            var index = this.listeners[type].indexOf(listener);
            this.listeners[type].splice(index, 1);
        }
    };
    module.exports.TypePublisher = TypePublisher;

    function OpenTypePublisher() {
        this.listeners = [];
    }
    OpenTypePublisher.prototype = TypePublisher.prototype;
    OpenTypePublisher.prototype.publish = publishType;
    module.exports.OpenTypePublisher = OpenTypePublisher;

    function OpenPublisher() {
        this.listeners = [];
        this.typeListeners = [];
        this.onceListeners = [];
        this.onceTypeListeners = [];
    }

    function isValidType(type) {
        return typeof type === 'string' || typeof type === 'number';
    }

    OpenPublisher.prototype.publish = function (type, data) {
        if (typeof data === 'undefined') {
            data = type;
            type = undefined;
        }
        this.listeners
            .concat(this.onceListeners)
            .filter(function (listener) {
                return listener != null;
            })
            .forEach(function (listener) {
                return listener(data);
            });
        this.onceListeners = [];

        if (!isValidType(type) && isValidType(data.type)) {
            type = data.type;
        }

        if (isValidType(type)) {
            const typeListeners = this.typeListeners[type] ? this.typeListeners[type] : [];
            const onceTypeListeners = this.onceTypeListeners[type] ? this.onceTypeListeners[type] : [];
            typeListeners
                .concat(onceTypeListeners)
                .filter(function (listener) {
                    return listener != null;
                })
                .forEach(function (listener) {
                    return listener(data);
                });
            delete this.onceTypeListeners[type];
        }
    };

    OpenPublisher.prototype.subscribe = function (type, listener) {
        if (typeof type === 'function') {
            listener = type;
            type = undefined;
        }
        if (isValidType(type)) {
            if (!this.typeListeners[type]) {
                this.typeListeners[type] = [];
            }
            this.typeListeners[type].push(listener);
        } else {
            this.listeners.push(listener);
        }
    };

    OpenPublisher.prototype.subscribeOnce = function (type, listener) {
        if (typeof type === 'function') {
            listener = type;
            type = undefined;
        }
        if (isValidType(type)) {
            if (!this.onceTypeListeners[type]) {
                this.onceTypeListeners[type] = [];
            }
            this.onceTypeListeners[type].push(listener);
        } else {
            this.onceListeners.push(listener);
        }
    };

    OpenPublisher.prototype.unsubscribe = function (type, callback) {
        if (typeof type === 'function') {
            callback = type;
            type = undefined;
        }
        var index;
        var array;
        if (isValidType(type)) {
            if (this.typeListeners[type]) {
                array = this.typeListeners[type]
            } else {
                if (this.onceTypeListeners[type]) {
                    array = this.onceTypeListeners[type];
                } else {
                    console.error('type do not exists', type);
                    throw 'type do not exists';
                }
            }
        } else {
            index = this.listeners.indexOf(callback);
            if (index == -1) {
                array = this.onceListeners;
            } else {
                array = this.listeners;
            }
        }

        index = array.indexOf(callback);
        if (index == -1) {
            console.error('subscriber do not exists', type, callback);
            throw 'subscriber do not exists';
        } else {
            delete array[index];
        }
    };

    module.exports.OpenPublisher = OpenPublisher;
});

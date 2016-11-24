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
            if (state === this._value) {
                return;
            }
            this._value = state;
            publish.call(this, state);
            var givenStateListeners = this.stateListeners[state];
            if (givenStateListeners && givenStateListeners.length > 0) {
                givenStateListeners.forEach(function (listener) {
                    return listener();
                });
            }
        }.bind(this);
        callback(newPublish);
    }

    StatePublisher.prototype = StreamPublisher.prototype;
    StatePublisher.prototype.subscribeState = function (type, listener) {
        if (!this.stateListeners[type]) {
            this.stateListeners[type] = [];
        }
        this.stateListeners[type].push(listener);
    };
    StatePublisher.prototype.unsubscribeState = function (type, listener) {
        if (!this.stateListeners[type]) return;
        var index = this.stateListeners[type].indexOf(listener);
        this.stateListeners[type].splice(index, 1);
    };
    StatePublisher.prototype.subscribeAndTrigger = function (listener) {
        this.subscribe(listener);
        listener(this._value);
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
        if (!this.listeners.has(type)) return;
        this.listeners.get(type).forEach(function (listener) {
            return listener(event);
        });
    }

    function TypePublisher(callback) {
        this.listeners = new Map();
        callback(publishType.bind(this));
    }

    TypePublisher.prototype = {
        subscribe: function (type, listener) {
            if (!this.listeners.has(type)) {
                this.listeners.set(type, []);
            }
            this.listeners.get(type).push(listener);
        },
        subscribeOnce: function (type, listener) {
            this.subscribe(type, function (event) {
                listener(event);
                this.unsubscribe(type, listener);
            }.bind(this));
        },
        unsubscribe: function (type, listener) {
            if (!this.listeners.has(type)) return;
            var index = this.listeners.get(type).indexOf(listener);
            this.listeners.get(type).splice(index, 1);
        }
    };
    module.exports.TypePublisher = TypePublisher;

    function OpenTypePublisher() {
        this.listeners = new Map();
    }
    OpenTypePublisher.prototype = TypePublisher.prototype;
    OpenTypePublisher.prototype.publish = publishType;
    module.exports.OpenTypePublisher = OpenTypePublisher;

    function OpenPublisher() {
        this.listeners = [];
        this.typeListeners = new Map();
        this.onceListeners = [];
        this.onceTypeListeners = new Map();
        this.lastListeners = [];
        this.lastTypeListeners = new Map();
    }

    function isValidType(type) {
        return typeof type === 'string' || typeof type === 'number' || typeof type === 'function';
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
        this.lastListeners
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
            const typeListeners = this.typeListeners.has(type) ? this.typeListeners.get(type) : [];
            const onceTypeListeners = this.onceTypeListeners.has(type) ? this.onceTypeListeners.get(type) : [];
            const lastTypeListeners = this.lastTypeListeners.has(type) ? this.lastTypeListeners.get(type) : [];
            typeListeners
                .concat(onceTypeListeners)
                .filter(function (listener) {
                    return listener != null;
                })
                .forEach(function (listener) {
                    return listener(data);
                });
            lastTypeListeners
                .filter(function (listener) {
                    return listener != null;
                })
                .forEach(function (listener) {
                    return listener(data);
                });
            this.onceTypeListeners.delete(type);
        }
    };

    OpenPublisher.prototype.subscribe = function (type, listener) {
        if (listener == null) {
            listener = type;
            type = undefined;
        }
        if (isValidType(type)) {
            if (!this.typeListeners.has(type)) {
                this.typeListeners.set(type, []);
            }
            this.typeListeners.get(type).push(listener);
        } else {
            this.listeners.push(listener);
        }
    };

    OpenPublisher.prototype.subscribeLast = function (type, listener) {
        if (isValidType(type)) {
            if (!this.lastTypeListeners.has(type)) {
                this.lastTypeListeners.set(type, []);
            }
            this.lastTypeListeners.get(type).push(listener);
        } else {
            this.lastListeners.push(listener);
        }
    };

    OpenPublisher.prototype.subscribeOnce = function (type, listener) {
        if (listener == null) {
            listener = type;
            type = undefined;
        }
        if (isValidType(type)) {
            if (!this.onceTypeListeners.has(type)) {
                this.onceTypeListeners.set(type, []);
            }
            this.onceTypeListeners.get(type).push(listener);
        } else {
            this.onceListeners.push(listener);
        }
    };

    OpenPublisher.prototype.unsubscribe = function (type, callback) {
        if (callback == null) {
            callback = type;
            type = undefined;
        }
        var index;
        var array;
        if (isValidType(type)) {
            if (this.typeListeners.has(type)) {
                array = this.typeListeners.get(type);
            } else {
                if (this.onceTypeListeners.has(type)) {
                    array = this.onceTypeListeners.get(type);
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

    module.exports.map = (publisher, map) => {
        return new StreamPublisher(push => {
            publisher.subscribe(value => push(map(value)))
        });
    };

    module.exports.merge = (...publishers) => {
        return new StreamPublisher(push => {
            const state = new Array(publishers.length).fill(null);
            const updateValue = (i, value) => {
                state[i] = value;
                push(state);
            };
            for (let i=0; i < publishers.length; i++) {
                publishers[i].subscribe(value => updateValue(i, value));
            }
        });   
    }
});

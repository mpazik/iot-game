(function () {
    if (!String.prototype.format) {
        String.prototype.format = function () {
            var args = arguments;
            return this.replace(/{(\d+)}/g, function (match, number) {
                return typeof args[number] != 'undefined' ? args[number] : match;
            });
        };
    }

    if (!Array.prototype.flatMap) {
        Object.defineProperty(Array.prototype, 'flatMap', {
            value: function (transform) {
                if (this == null) {
                    throw new TypeError('Array.prototype.findIndex called on null or undefined');
                }
                if (typeof transform !== 'function') {
                    throw new TypeError('predicate must be a function');
                }
                var list = Object(this);
                var length = list.length >>> 0;
                var thisArg = arguments[1];
                var value;
                var newFullArray = [];
                for (var i = 0; i < length; i++) {
                    value = list[i];
                    newFullArray = newFullArray.concat(transform.call(thisArg, value, i));
                }
                return newFullArray;
            },
            enumerable: false,
            configurable: false
        });
    }

    Array.prototype.clear = function () {
        'use strict';
        this.length = 0;
    };

    if (!Array.prototype.includes) {
        Array.prototype.includes = function (searchElement) {
            'use strict';
            var O = Object(this);
            var len = parseInt(O.length) || 0;
            if (len === 0) {
                return false;
            }
            var n = parseInt(arguments[1]) || 0;
            var k;
            if (n >= 0) {
                k = n;
            }
            else {
                k = len + n;
                if (k < 0) {
                    k = 0;
                }
            }
            var currentElement;
            while (k < len) {
                currentElement = O[k];
                if (searchElement === currentElement ||
                    (searchElement !== searchElement && currentElement !== currentElement)) {
                    return true;
                }
                k++;
            }
            return false;
        };
    }
    if (!Array.prototype.remove) {
        Object.defineProperty(Array.prototype, 'remove', {
            value: function (predicate) {
                for (var i = 0; i < this.length; i++) {
                    var value = this[i];
                    if (predicate(value)) {
                        this.splice(i, 1);
                        i--;
                    }
                }
            },
            enumerable: false,
            configurable: false
        });
    }
    Object.extend = function (target, source) {
        for (var prop in source) {
            if (!source.hasOwnProperty(prop)) {
                continue;
            }
            if (typeof source[prop] === 'object'&& target.hasOwnProperty(prop) && (typeof target[prop] === 'object')) {
                Object.extend(target[prop], source[prop]);
            } else {
                target[prop] = source[prop];
            }
        }
    };

    Object.values = function (obj) {
        return Object.keys(obj).map(function (key) {
            return obj[key]
        });
    };

    Object.forEach = function (obj, callable) {
        Object.keys(obj).forEach(function (key) {
            callable(obj[key], key);
        });
    };

    Object.map = function (obj, transform) {
        const result = {};
        Object.keys(obj).forEach(function (key) {
            result[key] = transform(obj[key], key);
        });
        return result;
    };

    Object.filter = function (obj, predicate) {
        const result = {};
        Object.keys(obj).forEach(function (key) {
            if (predicate(obj[key], key)) {
                result[key] = obj[key];
            }
        });
        return result;
    };

    Map.fromObject = function (object) {
        const result = new Map();
        Object.forEach(object, (value, key) => {
            result.set(key, value);
        });
        return result;
    };

    Map.prototype.addFromObject = function (object) {
        Object.forEach(object, (value, key) => {
            this.set(key, value);
        });
    };

    Map.toObject = function (map) {
        const result = {};
        map.forEach((value, key) => result[key] = value);
        return result;
    };

    Map.prototype.toObject = function () {
        const result = {};
        this.forEach((value, key) => result[key] = value);
        return result;
    };

    Map.prototype.map = function (transform) {
        const result = new Map();
        for (var entry of this) {
            result.set(entry[0], transform(entry[1]));
        }
        return result;
    };

    Map.prototype.filterValues = function (predicate) {
        const result = [];
        for (var entry of this) {
            if (predicate(entry[1], entry[0]))
                result.push(entry[1]);
        }
        return result;
    };

    Map.prototype.forEach = function (callable) {
        for (var entry of this) {
            callable(entry[1], entry[0])
        }
    };
})();
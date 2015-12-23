Predicates = {};

Predicates.is = function (value) {
    return function (value2) {
        return value2 === value;
    }
};

Predicates.isSet = function () {
    return function (value) {
        return !!value;
    }
};
define(function (require, exports, module) {

    function ValidOutcome(value) {
        this.value = value;
    }

    ValidOutcome.prototype.isValid = function () {
        return true;
    };
    ValidOutcome.prototype.isNotValid = function () {
        return false;
    };
    ValidOutcome.prototype.getValue = function () {
        return this.value;
    };
    ValidOutcome.prototype.getError = function () {
        console.trace();
        throw "it is a valid outcome and does not contains any errors";
    };
    ValidOutcome.prototype.map = function (fun) {
        return new ValidOutcome(fun(this.value));
    };
    ValidOutcome.prototype.flatMap = function (fun) {
        return fun(this.value);
    };

    function ErrorOutcome(error) {
        this.error = error;
    }

    ErrorOutcome.prototype.isValid = function () {
        return false;
    };
    ErrorOutcome.prototype.isNotValid = function () {
        return true;
    };
    ErrorOutcome.prototype.getValue = function () {
        throw "it is an in valid outcome and does not contains any value";
        //noinspection UnreachableCodeJS
        return null;
    };
    ErrorOutcome.prototype.getError = function () {
        return this.error;
    };
    ErrorOutcome.prototype.map = function (fun) {
        return new ErrorOutcome(this.error);
    };
    ErrorOutcome.prototype.flatMap = function (fun) {
        return new ErrorOutcome(this.error);
    };

    module.exports = {
        errorMsg: function (errorMsg) {
            return new ErrorOutcome(new Error(errorMsg));
        },
        error: function (error) {
            return new ErrorOutcome(error);
        },
        castError: function (outcome) {
            return outcome.map(null);
        },
        ok: function (value) {
            return new ValidOutcome(value);
        }
    };
});
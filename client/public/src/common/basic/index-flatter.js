define(function (require, exports, module) {
    const Outcome = require('./outcome');

    function IndexFlatter(sizes) {
        this.sizes = sizes;
    }

    IndexFlatter.fromValues = function (values) {
        return new IndexFlatter(values.map(function (values) {
            return values.length;
        }));
    };
    IndexFlatter.prototype.to2d = function (index) {
        if (index < 0) {
            return Outcome.errorMsg("index: " + index + " should be greater than 0");
        }
        for (var i = 0; i < this.sizes.length; i++) {
            var size = this.sizes[i];
            if (index < size) {
                return Outcome.ok([i, index]);
            }
            index -= size;
        }
        return Outcome.errorMsg("index " + index + " out of bound offsets " + this.sizes);
    };
    IndexFlatter.prototype.toFlat = function (x, y) {
        var sumTillX = 0;
        if (x >= this.sizes.length) {
            return Outcome.errorMsg("x: " + x + " should be less than: " + this.sizes.length);
        }
        if (x < 0) {
            return Outcome.errorMsg("x: " + x + " should be greater than 0");
        }
        if (y < 0) {
            return Outcome.errorMsg("y: " + x + " should be greater than 0");
        }
        var i;
        for (i = 0; i < x; i++) {
            sumTillX += this.sizes[i];
        }
        if (y >= this.sizes[i]) {
            return Outcome.errorMsg("y: " + x + " should be less than: " + this.sizes[i]);
        }
        return Outcome.ok(sumTillX + y);
    };

    module.exports = IndexFlatter;
});
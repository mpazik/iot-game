define(function (require, exports, module) {

    function Point(x, y) {
        this.x = x;
        this.y = y;
    }

    function interpolate(ratio, v1, v2) {
        const dv = v2 - v1;
        return v1 + dv * ratio;
    }

    Point.fromObject = function (data) {
        return new Point(data.x, data.y);
    };

    Point.distanceSqr = (p1, p2) => {
        const dx = p2.x - p1.x;
        const dy = p2.y - p1.y;
        return dx * dx + dy * dy;
    };

    Point.distance = (p1, p2) => {
        return Math.sqrt(Point.distanceSqr(p1, p2));
    };

    Point.isInRange = (p1, p2, radius) => Point.distanceSqr(p1, p2) <= radius * radius;

    Point.interpolate = function (ratio, p1, p2) {
        var x = interpolate(ratio, p1.x, p2.x);
        var y = interpolate(ratio, p1.y, p2.y);
        return {x, y}
    };

    Point.angleFromTo = (p1, p2) => {
        const dx = p2.x - p1.x;
        const dy = p2.y - p1.y;
        return -Math.atan2(dx, dy);
    };

    Point.multiplyInPlace = (p, scalar) => {
        p.x *= scalar;
        p.y *= scalar;
        return p;
    };

    Point.equal = (p1, p2) => {
        return p1.x == p2.x && p1.y == p2.y;
    };

    Point.prototype.toString = function () {
        return '(' + this.x + ', ' + this.y + ')';
    };

    Point.prototype.plus = function (point) {
        return new Point(this.x + point.x, this.y + point.y);
    };

    Point.prototype.minus = function (point) {
        return new Point(this.x - point.x, this.y - point.y);
    };

    Point.prototype.multiply = function (scalar) {
        return new Point(this.x * scalar, this.y * scalar);
    };

    Point.prototype.divide = function (scalar) {
        return this.multiply(1.0 / scalar);
    };

    Point.prototype.dotProduct = function (point) {
        return this.x * point.x + this.y * point.y;
    };

    Point.prototype.dotProductSqr = function () {
        return this.x * this.x + this.y * this.y;
    };

    Point.prototype.crossProduct = function (point) {
        return this.x * point.y - this.y * point.y;
    };

    Point.prototype.distanceTo = function (point) {
        return Math.sqrt(this.distanceSqrTo(point));
    };

    Point.prototype.distanceSqrTo = function (point) {
        return Point.distanceSqr(this, point)
    };

    Point.prototype.isInRange = function (point, radius) {
        return Point.isInRange(this, point, radius)
    };

    module.exports = Point;
});
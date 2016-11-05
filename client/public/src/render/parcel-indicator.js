define(function (require, exports, module) {
    const Pixi = require('pixi');
    const parcelPixelSize = require('configuration').parcelSize * require('configuration').tileSize;
    const Parcel = require('../component/parcel');

    const layer = new Pixi.Container();
    var parcelIndicator;

    Parcel.currentParcelHighlighting.subscribe(function (highlight) {
        if (highlight) {
            const currentParcel = Parcel.currentParcel.value;
            parcelIndicator = new Pixi.Graphics();
            parcelIndicator.beginFill(0x74C0FF, 0.2);
            parcelIndicator.lineStyle(3, 0x74C0FF, 0.4);
            parcelIndicator.drawRect(currentParcel.x * parcelPixelSize, currentParcel.y * parcelPixelSize, parcelPixelSize, parcelPixelSize);
            layer.addChild(parcelIndicator)
        } else {
            layer.removeChild(parcelIndicator);
            parcelIndicator = null;
        }
    });

    module.exports = {
        init: function () {
            layer.removeChildren();
        },
        get layer() {
            return layer
        }
    };
});
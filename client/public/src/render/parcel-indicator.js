define(function (require, exports, module) {
    const Pixi = require('pixi');
    const parcelPixelSize = require('configuration').parcelSize * require('configuration').tileSize;
    const Parcel = require('../component/parcel');

    const layer = new Pixi.Container();
    var parcelIndicator;

    function highlightParcel(highlight, parcel) {
        if (highlight) {
            parcelIndicator = new Pixi.Graphics();
            parcelIndicator.beginFill(0x74C0FF, 0.2);
            parcelIndicator.lineStyle(3, 0x74C0FF, 0.4);
            parcelIndicator.drawRect(parcel.x * parcelPixelSize, parcel.y * parcelPixelSize, parcelPixelSize, parcelPixelSize);
            layer.addChild(parcelIndicator)
        } else {
            layer.removeChild(parcelIndicator);
            parcelIndicator = null;
        }
    }

    Parcel.currentParcelHighlighting.subscribe(function (highlight) {
        highlightParcel(highlight, Parcel.currentParcel.value);
    });

    Parcel.playerParcelHighlighting.subscribe(function (highlight) {
        highlightParcel(highlight, Parcel.playerParcel);
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
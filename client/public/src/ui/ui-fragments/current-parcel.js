define(function (require) {
    const Parcel = require('../../component/parcel');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('current-parcel', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created: function () {
            this.innerHTML = `<div>
    <span id="parcel-name" class="on-non-solid-background"></span>
    <button id="parcel-info-button"><span class="action-key-shortcut">I</span>nfo</button>
</div>`;
        },
        attached: function () {
            Parcel.currentParcel.subscribe(this._update);
            document.getElementById('parcel-info-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'parcel-window');
            });
        },
        _update(parcel) {
            document.getElementById('parcel-name').innerText = parcel.parcelName;
        }
    });
});
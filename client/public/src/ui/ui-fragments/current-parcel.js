define((require) => {
    const Parcel = require('../../store/parcel');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('current-parcel', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created () {
        },
        attached () {
            this._update();
            Parcel.currentParcel.subscribe(this._update.bind(this));
        },
        detached () {
            Parcel.currentParcel.unsubscribe(this._update.bind(this));
        },
        _update() {
            const parcel = Parcel.currentParcel.value;
            if (!parcel) {
                return;
            }
            const isPlayerParcel = Parcel.isPlayerOnOwnParcel.value;
            this.innerHTML = `<div>
    <span class="on-non-solid-background">${parcel.parcelName}</span>
    <button id="parcel-info-button"><span class="action-key-shortcut">I</span>nfo</button>
    ${isPlayerParcel ? `<button id="parcel-build-button"><span class="action-key-shortcut">B</span>uild</button>` : ''}
</div>`;
            document.getElementById('parcel-info-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'parcel-window');
            });

            if (isPlayerParcel) {
                document.getElementById('parcel-build-button').addEventListener('click', () => {
                    userEventStream.publish('toggle-window', 'building-window');
                });
            }
        }
    });
});
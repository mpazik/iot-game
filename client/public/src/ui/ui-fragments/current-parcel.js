define((require) => {
    const Predicates = require('../../common/predicates');
    const Parcel = require('../../store/parcel');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    function updateCurrentParcel(parcel) {
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

    return {
        key: 'current-parcel',
        type: 'fragment',
        requirements: {
            playerAlive: Predicates.is(true)
        },
        attached(element) {
            element.updateCurrentParcel = updateCurrentParcel.bind(element);
            Parcel.currentParcel.subscribeAndTrigger(element.updateCurrentParcel);
        },
        detached (element) {
            Parcel.currentParcel.unsubscribe(element.updateCurrentParcel);
        }
    };
});
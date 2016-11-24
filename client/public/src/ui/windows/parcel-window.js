define((require) => {
    const Predicates = require('../../common/predicates');
    const KeyCodes = require('../../common/key-codes');
    const Parcel = require('../../store/parcel');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    function showClaimLand(element) {
        element.innerHTML = `<form id="claim-land-form">
    <div><label>Name:</label><input type="text" maxlength="20" minlength="5" required pattern="[a-zA-Z0-9]+" id="claim-land-name"></div>
    <input type="submit" value="Claim land">
</form>`;
        const parcelName = document.getElementById('claim-land-name');
        parcelName.addEventListener('keydown', event => {
            if (event.keyCode != KeyCodes.ESC) {
                event.stopPropagation();
            }
        });
        document.getElementById('claim-land-form').addEventListener('submit', (event) => {
            event.preventDefault();
            userEventStream.publish('claim-land', parcelName.value);
            userEventStream.publish('toggle-window', 'parcel-window');
        });
    }

    return {
        key: 'parcel-window',
        type: 'window',
        activateKeyBind: KeyCodes.fromLetter('I'),
        requirements: {
            playerAlive: Predicates.is(true),
        },
        attached(element) {
            const parcel = Parcel.currentParcel.value;
            element.innerHTML = `<div>
<div class="form-group">
    <div><label>Name:</label><span>${parcel.parcelName}</span></div>
</div>
<div class="form-group">    
    <div><label>Cords:</label><span>x: ${parcel.x} y: ${parcel.y}</span></div>
</div>
    ${parcel.ownerName ? `
<div class="form-group">
    <div><label>Owner:</label><span>${parcel.ownerName}</span></div>
</div>
` : ''}
    ${Parcel.canClaimCurrentLand() ? `<button id="claim-land-button">Claim land</button>` : ''}
                    
</div>`;
            const claimLandButton = document.getElementById('claim-land-button');
            if (claimLandButton) {
                claimLandButton.addEventListener('click', () => {
                    showClaimLand(element);
                });
            }
            Parcel.highlightCurrentParcel(true);
        },
        detached () {
            Parcel.highlightCurrentParcel(false);
        }
    };
});
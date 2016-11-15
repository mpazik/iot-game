define(function (require) {
    const Parcel = require('../../store/parcel');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('parcel-window', {
        type: 'window',
        properties: {
            activateKeyBind: KEY_CODES.fromLetter('I'),
            requirements: {
                playerAlive: Predicates.is(true),
            }
        },
        created () {
            const parcel = Parcel.currentParcel.value;
            this.innerHTML = `<div>
<div class="form-group">
    <div><label>Name:</label><span>${parcel.parcelName}</span></div>
</div>
<div class="form-group">    
    <div><label>Cords:</label><span>x: ${parcel.x} y: ${parcel.y}</span></div>
</div>
    ${parcel.owner ? `
<div class="form-group">
    <div><label>Owner:</label><span>${parcel.ownerName}</span></div>
</div>
` : ''}
    ${Parcel.canClaimCurrentLand() ? `<button id="claim-land-button">Claim land</button>` : ''}
                    
</div>`;
        },
        attached () {
            const claimLandButton = document.getElementById('claim-land-button');
            if (claimLandButton) {
                claimLandButton.addEventListener('click', () => {
                    this._showClaimLand();
                });
            }
            Parcel.highlightCurrentParcel(true);
        },
        detached () {
            Parcel.highlightCurrentParcel(false);
        },
        _showClaimLand() {
            this.innerHTML = `<form id="claim-land-form">
    <div><label>Name:</label><input type="text" maxlength="20" minlength="5" required pattern="[a-zA-Z0-9]+" id="claim-land-name"></div>
    <input type="submit" value="Claim land">
</form>`;
            const parcelName = document.getElementById('claim-land-name');
            parcelName.addEventListener('keydown', event => {
                if (event.keyCode != KEY_CODES.ESC) {
                    event.stopPropagation();
                }
            });
            document.getElementById('claim-land-form').addEventListener('submit', (event) => {
                event.preventDefault();
                userEventStream.publish('claim-land', parcelName.value);
                userEventStream.publish('toggle-window', 'parcel-window');
            });
        }
    });
});
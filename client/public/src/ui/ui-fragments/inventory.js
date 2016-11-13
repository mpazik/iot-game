define(require => {
    require('../elements/action-socket');
    const uiState = require('../../store/ui-state');
    const itemById = require('../../store/resources').item;
    const Item = require('../../store/item');

    return createUiElement('player-inventory', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created() {
            this.innerHTML = '<h3 style="margin-top: 0">Inventory</h3><div class="item-list"></div>';
        },
        attached() {
            this._updateActive(uiState.playerItems.value);
            this.classList.add('area');
            uiState.playerItems.subscribe(this._updateActive.bind(this));
        },
        detached() {
            uiState.playerItems.unsubscribe(this._updateActive.bind(this));
        },
        _updateActive() {
            const itemList = this.getElementsByClassName('item-list')[0];

            itemList.innerHTML = Object.keys(Item.playerItems).map(function (itemId) {
                return `<div><span>${itemById(itemId).name}</span> : <span>${Item.playerItems[itemId]}</span></div>`
            }).join('');
        }
    });
});
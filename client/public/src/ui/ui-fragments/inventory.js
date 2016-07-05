define(function (require, exports, module) {
    require('../elements/action-socket');
    const uiState = require('../../store/ui-state');
    const itemById = require('../../store/resources').item;
    
    return createUiElement('player-inventory', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created: function () {
            this.innerHTML = '<div class="item-list"></div>';
        },
        attached: function () {
            this._updateActive(uiState.playerItems.value);
            this.classList.add('area');
            uiState.playerItems.subscribe(this._updateActive.bind(this));
        },
        detached: function () {
            uiState.playerItems.unsubscribe(this._updateActive.bind(this));
        },
        _updateActive: function (quantities) {
            const itemList = this.getElementsByClassName('item-list')[0];

            itemList.innerHTML = Object.keys(quantities).map(function (itemId) {
                return `<div><span>${itemById(itemId).name}</span> : <span>${quantities[itemId]}</span></div>`
            }).join('');
        }
    });
});
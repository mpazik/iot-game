define(function (require, exports, module) {
    require('components/elements/action-socket');
    createUiElement('player-inventory', {
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
            this._updateActive(this.uiState.playerItems.value);
            this.classList.add('area');
            this.uiState.playerItems.subscribe(this._updateActive.bind(this));
        },
        detached: function () {
            this.uiState.playerItems.unsubscribe(this._updateActive.bind(this));
        },
        _updateActive: function (quantities) {
            const itemList = this.getElementsByClassName('item-list')[0];

            const itemById = this.game.itemById;
            itemList.innerHTML = Object.keys(quantities).map(function (itemId) {
                return `<div><span>${itemById(itemId).name}</span> : <span>${quantities[itemId]}</span></div>`
            }).join('');
        }
    });
});
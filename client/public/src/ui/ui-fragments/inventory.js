define(require => {
    const itemById = require('../../store/resources').item;
    const Item = require('../../store/item');

    function updateItems() {
        const itemList = this.getElementsByClassName('item-list')[0];

        itemList.innerHTML = Object.keys(Item.playerItems).map(function (itemId) {
            return `<div><span>${itemById(itemId).name}</span> : <span>${Item.playerItems[itemId]}</span></div>`
        }).join('');
    }

    return {
        key: 'player-inventory',
        type: 'fragment',
        requirements: {
            playerAlive: Predicates.is(true)
        },
        template: '<h3 style="margin-top: 0">Inventory</h3><div class="item-list"></div>',
        classes: ['area'],
        attached(element) {
            element.updateItems = updateItems.bind(element);
            Item.itemsChange.subscribeAndTrigger(element.updateItems);
        },
        detached(element) {
            Item.itemsChange.unsubscribe(element.updateItems);
        },
    };
});
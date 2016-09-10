define(function (require) {
    const Friends = require('../../component/friends');

    return createUiElement('friends-window', {
        type: 'window',
        properties: {
            requirements: {
                friendsConnectionState: Predicates.is('connected')
            }
        },
        created: function () {
            this.innerHTML = `
<h1>Friends</h1> 
<div>
    
</div>
`;
        },
        attached: function () {
            this._update();
            Friends.friendshipPublisher.subscribe(this._update.bind(this));
        },
        detached: function () {
            Friends.friendshipPublisher.subscribe(this._update.bind(this));
        },
        _update: function () {
            const list = this.getElementsByTagName('div')[0];
            const friendList = Array.from(Friends.friends.values());
            list.innerHTML = friendList.join('\n');
        }
    });
});
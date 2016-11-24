define((require) => {
    const Friends = require('../../component/friends');

    function updateWindow() {
        const list = this.getElementsByTagName('div')[0];
        const friendList = Array.from(Friends.friends.values());
        list.innerHTML = friendList.join('\n');
    }

    return {
        key: 'friends-window',
        type: 'window',
        requirements: {
            friendsConnectionState: Predicates.is('connected')
        },
        template: `
<h1>Friends</h1> 
<div>
    
</div>
`,
        attached(element) {
            element.updateWindow = updateWindow.bind(element);
            Friends.friendshipPublisher.subscribeAndTrigger(element.updateWindow);
        },
        detached(element) {
            Friends.friendshipPublisher.subscribe(element.updateWindow);
        }
    };
});
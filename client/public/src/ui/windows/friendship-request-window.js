define(function (require) {
    const friendshipRequestPublisher = require('../../component/friends').friendshipRequestPublisher;
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('friendship-request-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            requirements: {
                friendshipRequest: Predicates.isSet()
            }
        },
        created: function () {
            this.innerHTML = `
<h1>Friendship request</h1> 
<p>
    Player <b>${friendshipRequestPublisher.value.nick}</b> want to be a friend with you. <br />
    Do you accept?
</p>
<div>
<button style="margin-right: 20px" id="accept-friendship">Accept</button><button id="reject-friendship">Reject</button>
</div>
`;
        },
        attached: function () {
            const acceptFriendshipButton = document.getElementById('accept-friendship');
            deffer(function () {
                acceptFriendshipButton.focus()
            });

            acceptFriendshipButton.addEventListener('click', () => {
                userEventStream.publish('accept-friendship-request', {userId: friendshipRequestPublisher.value.userId})
            });
            document.getElementById('reject-friendship').addEventListener('click', () => {
                userEventStream.publish('reject-friendship-request', {})
            });
        }
    });
});
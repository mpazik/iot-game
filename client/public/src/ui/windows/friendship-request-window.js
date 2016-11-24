define((require) => {
    const friendshipRequestPublisher = require('../../component/friends').friendshipRequestPublisher;
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return {
        key: 'friendship-request-window',
        type: 'window',
        autoDisplay: true,
        requirements: {
            friendshipRequest: Predicates.isSet()
        },
        attached(element) {
            element.innerHTML = `
<h1>Friendship request</h1> 
<p>
    Player <b>${friendshipRequestPublisher.value.nick}</b> want to be a friend with you. <br />
    Do you accept?
</p>
<div>
<button style="margin-right: 20px" id="accept-friendship">Accept</button><button id="reject-friendship">Reject</button>
</div>
`;
            const acceptFriendshipButton = document.getElementById('accept-friendship');
            deffer(acceptFriendshipButton.focus);

            acceptFriendshipButton.addEventListener('click', () => {
                userEventStream.publish('accept-friendship-request', {userId: friendshipRequestPublisher.value.userId})
            });
            document.getElementById('reject-friendship').addEventListener('click', () => {
                userEventStream.publish('reject-friendship-request', {})
            });
        }
    };
});
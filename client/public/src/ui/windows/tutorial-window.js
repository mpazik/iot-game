define(function (require) {
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const tutorialToDisplay = require('../../component/tutorial').toDisplay;

    return createUiElement('tutorial-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            requirements: {
                scenarioType: Predicates.is('open-world'),
                tutorialToDisplay: Predicates.isSet()
            }
        },
        created: function () {
            const tutorial = tutorialToDisplay.value;
            this.innerHTML = `
<h1>${tutorial['title']}</h1>
<p>${tutorial['content']}</p>
<button class="large">Close</button>
`;
        },
        attached: function () {
            const closeButton = this.getElementsByTagName('button')[0];
            closeButton.addEventListener('click', function () {
                userEventStream.publish('closed-tutorial-window', {});
            });
        }
    });
});
define(function (require) {
    const Tutorial = require('../../component/tutorial');

    return createUiElement('tutorial-window', {
        type: 'window',
        properties: {
            activateKeyBind: KEY_CODES.fromLetter('T'),
            requirements: {
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            const tutorial = Tutorial.tutorialToDisplay;
            this.innerHTML = `
<h1>${tutorial['title']}</h1>
<div>${tutorial['content']}</div>
`;
        },
        attached: function () {
        }
    });
});
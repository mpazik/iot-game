define(function () {
    return createUiElement('thank-you-window', {
        type: 'window',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            this.innerHTML = `
    <h3>Thank you for your feedback!</h3>
    <p>If you liked the game or you want to stay in touch click the <b>Like</b> button.</p>
    <iframe src="https://www.facebook.com/plugins/like.php?href=https%3A%2F%2Fwww.facebook.com%2Fislesoftales&width=181&layout=button&action=like&size=large&show_faces=true&share=true&height=65&appId=584554898422237" width="181" height="35" style="border:none;overflow:hidden" scrolling="no" frameborder="0" allowTransparency="true"></iframe>
`;
        },
        attached: function () {
        }
    });
});
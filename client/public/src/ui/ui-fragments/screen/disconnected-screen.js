define((require) => {
    const app = require('../../../component/application');

    return {
        key: 'disconnected-screen',
        type: 'fragment',
        requirements: {
            instanceState: Predicates.is('disconnected')
        },
        template: `
<p>Disconnected from the server.</p>
<p><button class="large" autofocus>Reconnect</button></p>`,
        classes: ['game-state'],
        attached(element) {
            const button = element.getElementsByTagName("button")[0];
            button.onclick = app.connect;
            deffer(button.focus);
        }
    }
});
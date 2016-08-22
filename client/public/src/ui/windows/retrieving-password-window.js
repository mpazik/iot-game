define(function (require) {
    const app = require('../../component/application');

    return createUiElement('retrieving-password-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            closeable: false,
            requirements: {
                applicationState: Predicates.is('retrieving-password')
            }
        },
        created: function () {
            this.innerHTML = `
<h3>Forgot your password?</h3>
<p>
Currently there is not system to retrieve your password. Sorry! <br />
Please send your request to change your password by <a href="mailto:poczta+password-reset@marekpazik.pl">email</a>.
</p>
    <p><a id="go-back"><- go back to login page</a></p>
`;
        },
        attached: function () {
            const goBack = document.getElementById('go-back');
            goBack.addEventListener('click', () => {
                app.connect();
            });
        }
    });
});
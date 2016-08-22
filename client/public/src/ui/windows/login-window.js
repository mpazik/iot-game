define(function (require) {
    const app = require('../../component/application');
    const userService = require('../../component/user-service');

    return createUiElement('login-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            closeable: false,
            requirements: {
                applicationState: Predicates.is('need-authentication')
            }
        },
        created: function () {
            this.innerHTML = `
<form>
    <label>
        Nick:
        <input type="text" class="user-nick" minlength="3" maxlength="20" required>
        <br />
        Password:
        <input type="text" class="user-password" minlength="3" maxlength="20" required>
    </label>
    <input type="submit" value="Login!">
    <div class="error-message error"></div>
</form>
`;
        },
        attached: function () {
            const form = this.getElementsByTagName("form")[0];
            const userNickElement = form.getElementsByClassName('user-nick')[0];
            const userPasswordElement = form.getElementsByClassName('user-password')[0];
            const errorMessage = form.getElementsByClassName('error-message')[0];
            errorMessage.style.display = 'none';

            function showError(error) {
                errorMessage.innerText = error;
                errorMessage.style.display = 'block';
            }

            deffer(function () {
                userNickElement.focus();
            });

            form.onsubmit = function () {
                const nick = userNickElement.value;
                const password = userPasswordElement.value;
                userService.login(nick, password).then(() => {
                    errorMessage.style.display = 'none';
                    app.connect();
                }).catch(showError);
                return false;
            }.bind(this);
        }
    });
});
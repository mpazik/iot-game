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
<form id="login-form" method="post">
    <label for="login-nick">Nick:</label>
    <input type="text" name="login-nick" minlength="3" maxlength="20" required>
    <label for="login-password">Password:</label>
    <input type="password" name="login-password" minlength="3" maxlength="20" required>
    <br />
    <input type="submit" value="Login!">
</form>
<form id="registration-form" method="post">
    <label for="register-nick">Nick:</label>
    <input type="text" name="register-nick" minlength="3" maxlength="20" required>
    <label for="register-email">Email:</label>
    <input type="email" name="register-email" minlength="3" maxlength="20" required>
    <label for="register-password">Password:</label>
    <input type="password" name="register-password" minlength="3" maxlength="20" required>
    <label for="register-repeat-password">Repeat Password:</label>
    <input type="password" name="register-repeat-password" minlength="3" maxlength="20" required>
    <br />
    <input type="submit" value="Register & Play!">
</form>
<div style="clear: both;"></div>
<div id="error-message" class="error"></div>
`;
        },
        attached: function () {
            const errorMessage = document.getElementById('error-message');
            errorMessage.style.display = 'none';
            function showError(error) {
                errorMessage.innerText = error;
                errorMessage.style.display = 'block';
            }

            const loginForm = document.getElementById("login-form");
            const loginNickElement = document.getElementsByName('login-nick')[0];

            const loginPasswordElement = document.getElementsByName('login-password')[0];

            deffer(function () {
                loginNickElement.focus();
            });

            loginForm.onsubmit = function () {
                const nick = loginNickElement.value;
                const password = loginPasswordElement.value;
                userService.login(nick, password).then(() => {
                    errorMessage.style.display = 'none';
                    app.connect();
                }).catch(showError);
                return false;
            }.bind(this);

            const registerForm = document.getElementById("registration-form");
            const registerNickElement = document.getElementsByName('register-nick')[0];
            const registerEmailElement = document.getElementsByName('register-email')[0];
            const registerPasswordElement = document.getElementsByName('register-password')[0];
            const registerRepeatPasswordElement = document.getElementsByName('register-repeat-password')[0];

            registerForm.onsubmit = function () {
                const nick = registerNickElement.value;
                const email = registerEmailElement.value;
                const password = registerPasswordElement.value;
                const repeatedPassword = registerRepeatPasswordElement.value;

                if (password != repeatedPassword) {
                    showError("Passwords are not identical");
                    return false;
                }

                userService.register(nick, email, password).then(() => {
                    userService.login(nick, password).then(() => {
                        errorMessage.style.display = 'none';
                        app.connect();
                    }).catch(showError);
                }).catch(showError);
                return false;
            }.bind(this);
        }
    });
});
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
    <div class="form-group">
        <label for="login-nick">Nick:</label>
        <input type="text" name="login-nick" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <label for="login-password">Password:</label>
        <input type="password" name="login-password" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <input type="submit" value="Login!">
    </div>
    <div class="form-group">
        <br />
        <a id="forgot-your-password">Forgot your password?</a>
    </div>
    <div class="form-group">
        <div id="login-error-message" class="error"></div>
    </div>
</form>
<form id="registration-form" method="post">
    <div class="form-group">
        <label for="register-nick">Nick:</label>
        <input type="text" name="register-nick" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <label for="register-email">Email:</label>
        <input type="email" name="register-email" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <label for="register-password">Password:</label>
        <input type="password" name="register-password" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <label for="register-repeat-password">Repeat Password:</label>
        <input type="password" name="register-repeat-password" minlength="3" maxlength="20" required>
    </div>
    <div class="form-group">
        <input type="submit" value="Register & Play!">
    </div>
    <div class="form-group">
        <div id="register-error-message" class="error"></div>
    </div>
</form>

`;
        },
        attached: function () {
            const forgotYourPassword = document.getElementById('forgot-your-password');
            forgotYourPassword.addEventListener('click', () => {
                app.retrievingPassword();
            });

            const loginErrorMessage = document.getElementById('login-error-message');
            const registerErrorMessage = document.getElementById('register-error-message');
            loginErrorMessage.style.display = 'none';
            registerErrorMessage.style.display = 'none';

            function showLoginError(error) {
                loginErrorMessage.innerText = error;
                loginErrorMessage.style.display = 'block';
                registerErrorMessage.style.display = 'block';
            }

            function showRegisterError(error) {
                registerErrorMessage.innerText = error;
                loginErrorMessage.style.display = 'block';
                registerErrorMessage.style.display = 'block';
            }

            const loginForm = document.getElementById("login-form");
            const loginNickElement = document.getElementsByName('login-nick')[0];
            const loginPasswordElement = document.getElementsByName('login-password')[0];

            loginForm.onsubmit = function () {
                const nick = loginNickElement.value;
                const password = loginPasswordElement.value;
                userService.login(nick, password).then(() => {
                    loginErrorMessage.style.display = 'none';
                    app.connect();
                }).catch(showLoginError);
                return false;
            }.bind(this);

            deffer(function () {
                loginNickElement.focus();
            });

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
                    showRegisterError("Passwords are not identical");
                    return false;
                }

                userService.register(nick, email, password).then(() => {
                    userService.login(nick, password).then(() => {
                        loginErrorMessage.style.display = 'none';
                        app.connect();
                    }).catch(showRegisterError);
                }).catch(showRegisterError);
                return false;
            }.bind(this);
        }
    });
});
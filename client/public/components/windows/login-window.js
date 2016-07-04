define(function (require, exports, module) {
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
    </label>
    <input type="submit" value="Login!">
    <div class="error-message error"></div>
</form>
`;
        },
        attached: function () {
            const game = this.game;
            const form = this.getElementsByTagName("form")[0];
            const userNickElement = form.getElementsByClassName('user-nick')[0];
            const errorMessage = form.getElementsByClassName('error-message')[0];
            errorMessage.style.display = 'none';

            function showError(error) {
                errorMessage.innerText = error;
                errorMessage.style.display = 'block';
            }

            deffer(function () {
                userNickElement.focus();
            });

            form.onsubmit = function (event) {
                const nick = userNickElement.value;
                Request.Server.canPlayerLogin(nick).then(function () {
                    errorMessage.style.display = 'none';
                    game.setUser(nick);
                    game.connect();
                }).catch(function (error) {
                    showError(error);
                });
                return false;
            }.bind(this);
        }
    });
});
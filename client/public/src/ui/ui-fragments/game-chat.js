define(function (require, exports, module) {
    const uiState = require('../../store/ui-state');
    const chat = require('../../component/chat');

    return createUiElement('game-chat', {
        type: 'fragment',
        properties: {
            requirements: {
                applicationState: Predicates.is('running')
            }
        },
        created: function () {
            this.innerHTML = `
<ul class="chat-messages"></ul>
<input class="chat-input" type="text" title="message">
`;
        },
        attached: function () {

            this.messages = this.getElementsByClassName('chat-messages')[0];
            const input = this.getElementsByClassName('chat-input')[0];
            const element = this;
            this.fadeTimeout = null;
            this.hideTimeout = null;

            document.addEventListener('keydown', function (event) {
                function isElementClickable(element) {
                    const tag = element.tagName.toLowerCase();
                    return ['button', 'a', 'input'].includes(tag)
                }

                if (event.keyCode == KEY_CODES.ENTER) {
                    if (isElementClickable(document.activeElement)) {
                        return;
                    }
                    element.style.display = 'block';
                    input.focus();
                }
            });
            input.addEventListener('keydown', function (event) {
                if (event.keyCode == KEY_CODES.ESC) {
                    input.blur();
                    event.stopPropagation()
                }
                if (event.keyCode == KEY_CODES.ENTER) {
                    if (input.value.length > 0) {
                        chat.sendMessage(input.value);
                    }
                    input.value = '';
                    input.blur();
                }
                event.stopPropagation()
            });

            input.addEventListener("focus", this._showChat.bind(this));
            input.addEventListener("blur", this._fadeDelayed.bind(this));

            uiState.playerMessage.subscribe(this._print.bind(this))
        },
        detached: function () {
            uiState.playerMessage.unsubscribe(this._print.bind(this))
        },
        _print: function (data) {
            var line = document.createElement('li');
            line.innerHTML = `<span class="player">${data.playerNick}</span>: <span class="message">${data.message}</span>`;
            this.messages.appendChild(line);
            this.messages.scrollTop = this.messages.scrollHeight;
            this._showChat();
        },
        _fadeDelayed: function () {
            const chat = this;
            chat.fadeTimeout = setTimeout(function () {
                chat.style.animation = 'fade 1s linear';
                chat.hideTimeout = setTimeout(function () {
                    chat.style.display = 'none';
                    chat.style.animation = 'none';
                }, 1000);
            }, 10000);
        },
        _showChat: function () {
            this.style.display = 'block';
            clearTimeout(this.fadeTimeout);
            clearTimeout(this.hideTimeout);
            this._fadeDelayed();
        }
    });
});
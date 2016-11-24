define((require) => {
    const Predicates = require('../../common/predicates');
    const KeyCodes = require('../../common/key-codes');
    const chat = require('../../component/chat');

    function showChat(element) {
        element.style.display = 'block';
        clearTimeout(element.fadeTimeout);
        clearTimeout(element.hideTimeout);
        if (!element.isFocused) {
            fadeDelayed(element);
        }
    }

    function fadeDelayed(element) {
        element.fadeTimeout = setTimeout(() => {
            element.style.animation = 'fade 1s linear';
            element.hideTimeout = setTimeout(() => {
                element.style.display = 'none';
                element.style.animation = 'none';
            }, 1000);
        }, 3000);
    }

    return {
        key: 'game-chat',
        type: 'fragment',
        requirements: {
            chatState: Predicates.is('connected')
        },
        isFocused: false,
        template: `
<ul class="chat-messages"></ul>
<input class="chat-input" type="text" title="message">
`,
        attached(element) {
            element.fadeTimeout = null;
            element.hideTimeout = null;
            const input = element.getElementsByClassName('chat-input')[0];

            document.addEventListener('keydown', function (event) {
                function canElementBeClicked(element) {
                    const tag = element.tagName.toLowerCase();
                    return ['button', 'a', 'input'].includes(tag)
                }

                if (event.keyCode == KeyCodes.ENTER) {
                    if (canElementBeClicked(document.activeElement)) {
                        return;
                    }
                    element.style.display = 'block';
                    input.focus();
                }
            });
            input.addEventListener('keydown', function (event) {
                if (event.keyCode == KeyCodes.ESC) {
                    input.blur();
                    event.stopPropagation()
                }
                if (event.keyCode == KeyCodes.ENTER) {
                    if (input.value.length > 0) {
                        chat.send(input.value);
                    }
                    input.value = '';
                    input.blur();
                }
                event.stopPropagation()
            });

            input.addEventListener("focus", () => {
                element.isFocused = true;
                showChat(element);
            });
            input.addEventListener("blur", () => {
                element.isFocused = false;
                fadeDelayed(element);
            });

            const messagesElement = element.getElementsByClassName('chat-messages')[0];
            element.printMessage = (message) => {
                var line = document.createElement('li');
                line.innerHTML = `<span class="message message-type-${message.type}">${message.text}</span>`;
                messagesElement.appendChild(line);
                messagesElement.scrollTop = messagesElement.scrollHeight;
                showChat(element);
            };
        },
        detached(element) {
            chat.chatMessage.unsubscribe(element.printMessage)
        }
    }
});
define(function (require) {
    const customCursor = require('../../store/ui-state').customCursor;

    let wasInitiated = false;
    let element;

    function updateCursorPosition(event) {
        if (!wasInitiated) {
            element.setAttribute('class', 'icon-' + customCursor.value);
            document.body.style.cursor = 'none';
            wasInitiated = true;
        }
        const cursor = document.getElementById('custom-cursor');
        cursor.style.left = (event.clientX - 24) + 'px';
        cursor.style.top = (event.clientY - 24) + 'px';
    }

    return createUiElement('custom-cursor', {
        type: 'fragment',
        properties: {
            requirements: {
                customCursor: Predicates.isSet()
            }
        },
        created: function () {
        },
        attached: function () {
            element = this;
            wasInitiated = false;
            this.setAttribute('id', 'custom-cursor');
            document.addEventListener("mousemove", updateCursorPosition);
        },
        detached: function () {
            document.body.style.cursor = 'default';
            document.removeEventListener("mousemove", updateCursorPosition);
        },
    });
});
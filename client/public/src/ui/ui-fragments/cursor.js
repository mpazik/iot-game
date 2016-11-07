define(function (require) {
    const customCursor = require('../../store/ui-state').customCursor;

    function updateCursorPosition(event) {
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
            this.setAttribute('id', 'custom-cursor');
            this.setAttribute('class', 'icon-' + customCursor.value);
            document.body.style.cursor = 'none';
            document.addEventListener("mousemove", updateCursorPosition);
        },
        detached: function () {
            document.body.style.cursor = 'default';
            document.removeEventListener("mousemove", updateCursorPosition);
        },
    });
});
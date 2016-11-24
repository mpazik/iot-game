define((require) => {
    const Predicates = require('../../common/predicates');
    const customCursor = require('../../store/ui-state').customCursor;

    function updateCursorPosition(event) {
        if (!this.wasInitiated) {
            this.setAttribute('class', 'icon-' + customCursor.value);
            document.body.style.cursor = 'none';
            this.wasInitiated = true;
        }
        const cursor = document.getElementById('custom-cursor');
        cursor.style.left = (event.clientX - 24) + 'px';
        cursor.style.top = (event.clientY - 24) + 'px';
    }

    return {
        key: 'custom-cursor',
        type: 'fragment',
        requirements: {
            customCursor: Predicates.isSet()
        },
        attached(element) {
            element.updateCursorPosition = updateCursorPosition.bind(element);
            document.addEventListener("mousemove", element.updateCursorPosition);
        },
        detached(element) {
            document.body.style.cursor = 'default';
            document.removeEventListener("mousemove", element.updateCursorPosition);
        },
    };
});
const UiElement = Object.create(HTMLElement.prototype, {
    createdCallback: {
        value: function () {
            if (this.created) {
                this.created();
            }
        }
    },
    attachedCallback: {
        value: function () {
            if (this.attached) {
                this.attached();
            }
        }
    },
    detachedCallback: {
        value: function () {
            if (this.detached) {
                this.detached();
            }
        }
    },
    attributeChangedCallback: {
        value: function (attrName, oldVal, newVal) {
            if (this.attributeChanged) {
                this.attributeChanged(attrName, oldVal, newVal);
            }
        }
    }
});

function createUiElement(key, definition) {
    // const fragmentPrototype = Object.create(UiElement);
    // const element = document.createElement('div');
    // element.definition = definition;
    // element.key = key;
    // element.setAttribute('id', key);
    definition.key = key;
    return definition;
}
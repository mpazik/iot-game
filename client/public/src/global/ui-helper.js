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

function createUiElement(key, prototype) {
    const fragmentPrototype = Object.create(UiElement);
    Object.extend(fragmentPrototype, prototype);
    return document.registerElement(key, {prototype: fragmentPrototype});
}
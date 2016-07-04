UiElement = Object.create(HTMLElement.prototype, {
    createdCallback: {
        value: function () {
            if (this.created) {
                this.created();
            }
        }
    },
    attachedCallback: {
        value: function () {
            function findUiElement(element) {
                const parent = element.parentNode;
                if (parent == null) {
                    throw 'Ui element has to be defined in the \'game-ui\' element';
                }
                if (parent.tagName == 'GAME-UI') {
                    return parent;
                }
                return findUiElement(parent);
            }

            this.ui = findUiElement(this);

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
    },
    game: {
        get: function () {
            return this.ui.game;
        }
    },
    uiState: {
        get: function () {
            return this.ui.game.uiState;
        }
    }
});

UiElements = {};

function createUiElement(key, prototype) {
    const fragmentPrototype = Object.create(UiElement);
    Object.extend(fragmentPrototype, prototype);
    UiElements[key] = document.registerElement(key, {prototype: fragmentPrototype});
}
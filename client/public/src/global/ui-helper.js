UiElement = Object.create(HTMLElement.prototype, {
    createdCallback: {
        value: function () {
            var mainElement = document.importNode(this._template, true);
            this.appendChild(mainElement);

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
    const ownerDocument = document.currentScript.ownerDocument;
    const fragmentPrototype = Object.create(UiElement);
    const template = ownerDocument.getElementById(key);
    fragmentPrototype._template = template.content;
    Object.extend(fragmentPrototype, prototype);
    UiElements[key] = document.registerElement(key, {prototype: fragmentPrototype});
}
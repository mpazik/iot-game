define(function (require, exports, module) {
    const Pixi = require('pixi');
    require('pixi-spine');
    const Resources = require('../../store/resources');
    const CharacterType = require('../../store/character').CharacterType;
    const Dispatcher = require('../../component/dispatcher');

    const hoverFilter = new Pixi.filters.GrayFilter();
    hoverFilter.gray = -2.0;

    const interactiveFilter = new Pixi.filters.DropShadowFilter();
    interactiveFilter.blur = 10;
    interactiveFilter.angle = 0;
    interactiveFilter.distance = 0;
    interactiveFilter.alpha = 1;

    const healthBarLength = 50;
    const healthBarBgLength = 56;

    function CharacterModel(character, health) {
        this.id = character.id;
        Pixi.Container.call(this);

        this.rotatable = new Pixi.Container();
        this.addChild(this.rotatable);

        this.spine = new Pixi.spine.Spine(Resources.spine('player').spineData);
        this.spine.skeleton.setToSetupPose();
        this.rotatable.addChild(this.spine);

        this.createHpBar();
        this.updateHpBar(health);

        this.mousedown = function () {
            Dispatcher.userEventStream.publish({
                type: 'character-clicked',
                characterId: this.id
            });
        };

        //noinspection JSUnusedGlobalSymbols
        this.hitArea = new Pixi.Circle(0, 0, 40);

        //noinspection JSUnusedGlobalSymbols
        this.mouseover = function () {
            this.filters = [interactiveFilter, hoverFilter];
        };

        //noinspection JSUnusedGlobalSymbols
        this.mouseout = function () {
            this.filters = [interactiveFilter];
        };

        if (character.type === CharacterType.Player) {
            this.createNick(character.nick);
        }
        if (character.type === CharacterType.Bot) {
            const chest = this.spine.skeleton.findSlot('chest');
            chest.b = 0.5;
            chest.g = 0.5;
            this.spine.scale = {x: 0.9, y: 0.9};
        }
    }

    CharacterModel.prototype = Object.create(Pixi.Container.prototype);

    CharacterModel.prototype.makeInteractive = function () {
        this.interactive = true;
        this.filters = [interactiveFilter];
    };

    CharacterModel.prototype.makeNonInteractive = function () {
        if (this.interactive) {
            this.interactive = false;
            this.filters = null;
        }
    };

    CharacterModel.prototype.createHpBar = function () {
        var healthBarBg = new Pixi.Graphics();
        healthBarBg.beginFill(0x000000);
        healthBarBg.drawRect(-healthBarBgLength / 2, -36, healthBarBgLength, 10);
        this.addChild(healthBarBg);
        var healthBar = new Pixi.Graphics();
        healthBar.beginFill(0x00FF00);
        healthBar.drawRect(-healthBarLength / 2, -34, healthBarLength, 6);
        this.healthBar = healthBar;
        this.addChild(healthBar);
    };

    CharacterModel.prototype.updateHpBar = function (healthPerCent) {
        var width = healthBarLength * healthPerCent;
        this.healthBar.width = width;
        this.healthBar.x = -25 + width / 2;
    };

    CharacterModel.prototype.createNick = function (nickName) {
        const nick = new Pixi.Text(nickName, {
            font: "16px Arial",
            fill: 0xffffff
        });
        nick.position.x = -(nick.width / 2);
        nick.position.y = 20;
        this.addChild(nick);
    };

    module.exports = CharacterModel;
});
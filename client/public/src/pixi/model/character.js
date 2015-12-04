define(function (require, exports, module) {
    const Pixi = require('lib/pixi');
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

    function CharacterModel(character) {
        this.id = character.id;
        Pixi.Container.call(this);

        this.rotatable = new Pixi.Container();
        this.addChild(this.rotatable);
        this.createPart('rightShoe', "right-shoe.png", -9, 3);
        this.createPart('leftShoe', "left-shoe.png", 9, 3);
        this.createPart('rightHand', "hand.png", 18, 3);
        this.createPart('leftHand', "hand.png", -18, 3);
        this.createPart('chest', "chest.png", 0, 0);
        this.createPart('head', "head.png", 0, 2);
        this.hitArea = new Pixi.Circle(0, 0, 40);
        this.createHpBar();
        //this.updateHpBar(creature.health);

        this.mousedown = function () {
            Dispatcher.userEventStream.publish({
                type: 'character-clicked',
                characterId: this.id
            });
        };

        this.mouseover = function () {
            this.filters = [interactiveFilter, hoverFilter];
        };

        this.mouseout = function () {
            this.filters = [interactiveFilter];
        };

        if (character.type === CharacterType.Player) {
            this.createNick(character.nick);
        }
        if (character.type === CharacterType.Bot) {
            this.chest.tint = 0xFF7777;
            this.scale = {x: 0.9, y: 0.9};
        }

        //moveAnim = new TimeLine({
        //    paused: true
        //});
        //moveAnim.to(this.leftShoe.position, 0.25, {y: 11}, 0);
        //moveAnim.to(this.leftShoe.position, 0.5, {y: -5}, 0.25);
        //moveAnim.to(this.leftShoe.position, 0.25, {y: 3}, 0.75);
        //moveAnim.to(this.rightShoe.position, 0.25, {y: -5}, 0);
        //moveAnim.to(this.rightShoe.position, 0.5, {y: 11}, 0.25);
        //moveAnim.to(this.rightShoe.position, 0.25, {y: 3}, 0.75);
        //moveAnim.to(this.leftHand.position, 0.25, {y: 7}, 0);
        //moveAnim.to(this.leftHand.position, 0.5, {y: -1}, 0.25);
        //moveAnim.to(this.leftHand.position, 0.25, {y: 3}, 0.75);
        //moveAnim.to(this.rightHand.position, 0.25, {y: -1}, 0);
        //moveAnim.to(this.rightHand.position, 0.5, {y: 7}, 0.25);
        //moveAnim.to(this.rightHand.position, 0.25, {y: 3}, 0.75);
        //this.animation = {
        //    move: moveAnim
        //};
    }

    CharacterModel.prototype = Object.create(Pixi.Container.prototype);

    CharacterModel.prototype.createPart = function (name, img, x, y) {
        const part = new Pixi.Sprite.fromImage(img);
        part.anchor = new Pixi.Point(0.5, 0.5);
        part.position = new Pixi.Point(x, y);
        this.rotatable.addChild(part);
        this[name] = part;
    };

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
        var healthBarBg = new PIXI.Graphics();
        healthBarBg.beginFill(0x000000);
        healthBarBg.drawRect(-healthBarBgLength / 2, -36, healthBarBgLength, 10);
        this.addChild(healthBarBg);
        var healthBar = new PIXI.Graphics();
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
define(function (require, exports, module) {
    const Pixi = require('pixi');
    const Dispatcher = require('../../component/dispatcher');
    const tileImageSize = require('configuration').tileImageSize;
    const tileZoom = require('configuration').tileSize / tileImageSize;

    function CharacterModel(character) {
        this.id = character.id;
        Pixi.Container.call(this);

        this.rotatable = new Pixi.Container();
        this.addChild(this.rotatable);

        const characterTexture = Pixi.Texture.fromFrame('sprites/character.png');

        this.sprite = new AnimatedSprite(
            characterTexture,
            tileImageSize,
            tileImageSize * 2,
            characterAnimation
        );
        this.sprite.setState('lookDown');
        this.sprite.scale = {x: tileZoom, y: tileZoom};
        this.sprite.position.x = (-tileImageSize * tileZoom) / 2;
        // this.sprite.position.y = (-tileImageSize * tileZoom);
        this.rotatable.addChild(this.sprite);

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
        };

        //noinspection JSUnusedGlobalSymbols
        this.mouseout = function () {
        };

        if (character.nick) {
            this.createNick(character.nick);
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

    CharacterModel.prototype.createNick = function (nickName) {
        const nick = new Pixi.Text(nickName, {
            font: "16px Arial",
            fill: 0xffffff
        });
        nick.position.x = -(nick.width / 2);
        nick.position.y = -10;
        this.addChild(nick);
    };

    const characterAnimation = {
        lookDown: [0],
        lookUp: [4],
        lookRight: [8],
        lookLeft: [12],
        moveDown: [0, 1, 2, 3],
        moveUp: [4, 5, 6, 7],
        moveRight: [8, 9, 10, 11],
        moveLeft: [12, 13, 14, 15]
    };

    function AnimatedSprite(texture, frameWidth, frameHeight, animations) {
        this.frames = [];
        this.animations = animations;

        for (var i = 0; i < texture.height / frameHeight; i++) {
            for (var j = 0; j < texture.width / frameWidth; j++) {
                var frame = new Pixi.Texture(texture, {
                    x: j * frameWidth,
                    y: i * frameHeight,
                    width: frameWidth,
                    height: frameHeight
                });
                this.frames.push(frame);
            }
        }

        Pixi.Sprite.call(this, this.frames[0]);
    }

    AnimatedSprite.prototype = Object.create(Pixi.Sprite.prototype);
    AnimatedSprite.prototype._getCurrentFrame = function (time) {
        return Math.round((time - this.startTime) / 200);
    };
    AnimatedSprite.prototype.isFinished = function (time) {
        return this._getCurrentFrame(time) >= this.animations[this.state].length;
    };
    AnimatedSprite.prototype.setState = function (name, startTime) {
        this.startTime = startTime || 0;
        if (!this.animations.hasOwnProperty(name)) {
            throw `Animation state ${name} is undefined`
        }
        this.state = name;
        this.texture = this.frames[this.animations[this.state][0]]
    };
    AnimatedSprite.prototype.getState = function () {
        return this.state;
    };
    AnimatedSprite.prototype.update = function (time) {
        if (this.startTime == 0) {
            return;
        }
        const animationFrames = this.animations[this.state];
        const frameIndex = this._getCurrentFrame(time);
        if (frameIndex >= animationFrames.length) {
            this.texture = this.frames[animationFrames[0]];
            return;
        }
        this.texture = this.frames[animationFrames[frameIndex]]
    };

    module.exports = CharacterModel;
});
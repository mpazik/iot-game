define(function (require, exports, module) {
    const Pixi = require('pixi');
    const Configuration = require('configuration');
    const Items = require('../common/model/items').Ids;
    var assetsPath = Configuration.assetsLocalization + '/';
    var tilesets = {};
    var skills = {};
    var objectKinds = {};
    const items = {
        [Items.ARROW]: {name: 'Arrow'},
        [Items.STICK]: {name: 'Stick'}
    };

    const sprites = ["objects"];
    const spines = ["player"];

    function loadCss(path) {
        const head = document.getElementsByTagName('head')[0];
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = path;
        head.appendChild(link);
    }

    function loadAssets(files) {
        files.forEach(function (file) {
            Pixi.loader.add(file);
        });
        return new Promise(function (resolve) {
            Pixi.loader.load(resolve);
        });
    }

    function loadImage(name) {
        var absoluteUrl = assetsPath + name;
        Pixi.loader.add(name, absoluteUrl);
        return new Promise((resolve) => {
            Pixi.loader.load(function () {
                Pixi.utils.TextureCache[name] = Pixi.utils.TextureCache[absoluteUrl];
                resolve();
            });
        });
    }

    function loadSpines(spines) {
        spines.forEach(function (file) {
            const path = assetPath("spines", file);
            Pixi.loader.add(path);
        });
        return new Promise((resolve) => {
            Pixi.loader.load(resolve);
        });
    }

    function assetPath(asset, file) {
        return assetsPath + asset + "/" + file + ".json";
    }

    function loadJson(url) {
        var absoluteUrl = assetsPath + url + '.json';
        var xobj = new XMLHttpRequest();
        xobj.overrideMimeType("application/json");
        xobj.open('GET', absoluteUrl, true);
        return new Promise(function (resolve, reject) {
            xobj.onreadystatechange = function () {
                if (xobj.readyState == XMLHttpRequest.DONE) {
                    if (xobj.status == 200) {
                        var json = JSON.parse(xobj.responseText);
                        resolve(json);
                    }
                    else {
                        reject();
                    }
                }
            };
            xobj.send();
        });
    }

    function throwIfNull(id, element, name) {
        if (!element) {
            throw `${name}: ${id} has not been loaded.`;
        }
        return element;
    }

    module.exports = {
        skill: id => throwIfNull(id, skills[id], 'Skill'),
        item: id => throwIfNull(id, items[id], 'Item'),
        objectKind: id => throwIfNull(id, objectKinds[id], 'WorldObject'),
        tileset: name => throwIfNull(name, tilesets[name], 'TileSet'),
        spine: name => throwIfNull(name, Pixi.loader.resources[assetPath("spines", name)], 'Spine'),
        load: function () {
            loadCss(assetsPath + "icons/icons.css");
            const spritesPaths = sprites.map(function (file) {
                return assetPath("sprites", file);
            });

            return Promise.all([
                loadSpines(spines),
                loadAssets(spritesPaths),
                loadJson('tilesets/basic').then(function (tileset) {
                    tilesets[tileset.key] = tileset;
                    return loadImage("tilesets/" + tileset.image);
                }),
                loadJson('entities/objects').then(function (downloadedObjectKinds) {
                    downloadedObjectKinds.forEach(obj => objectKinds[obj.id] = obj)
                }),
                loadJson('skills').then(function (downloadedSkills) {
                    skills = downloadedSkills;
                })
            ]);
        }
    };
});
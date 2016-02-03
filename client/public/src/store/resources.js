define(function (require, exports, module) {
    var Pixi = require('pixi');
    var Configuration = require('configuration');
    var assetsPath = Configuration.assetsLocalization + '/';
    var tilesets = {};
    var skills = {};

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

    module.exports = {
        tileset: function (name) {
            if (tilesets[name] == null) {
                throw `Requested tileset ${name} some how has not been loaded.`;
            }
            return tilesets[name];
        },
        skill: function (id) {
            return skills[id];
        },
        spine: function (name) {
            const spine = Pixi.loader.resources[assetPath("spines", name)];
            if (!spine) {
                throw `Spine: ${name} was not loaded.`;
            }
            return spine;
        },
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
                loadJson('skills').then(function (downloadedSkills) {
                    skills = downloadedSkills;
                })
            ]);
        }
    };
});
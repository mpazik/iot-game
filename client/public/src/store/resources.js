define(function (require, exports, module) {
    var Pixi = require('lib/pixi');
    var dataServer = "http://localhost:8080/assets/";
    var tilesets = {};
    var skills = {};

    const sprites = ["player", "objects"];

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
        return new Promise(function (resolve, reject) {
            Pixi.loader.once("complete", resolve);
            Pixi.loader.load();
        });
    }

    function loadImage(name) {
        var absoluteUrl = dataServer + name;
        Pixi.loader.add(name, absoluteUrl);
        return new Promise(function (resolve, reject) {
            Pixi.loader.once('complete', function () {
                Pixi.utils.TextureCache[name] = Pixi.utils.TextureCache[absoluteUrl];
                resolve();
            });
            Pixi.loader.load();
        });
    }

    function loadJson(url) {
        var absoluteUrl = dataServer + url + '.json';
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
                throw "requested tileset " + name + " some how has not been loaded";
            }
            return tilesets[name];
        },
        skill: function (id) {
            return skills[id];
        },
        load: function () {
            loadCss(dataServer + "sprites/game-icons.css");
            const spritesPaths = sprites.map(function (file) {
                return dataServer + "sprites/" + file + ".json"
            });

            return Promise.all([
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
const skills = require('./skills').skills;
const fs = require('fs');

const pathToAssets = '../public/assets/';

const json = JSON.stringify(skills);
fs.writeFile(pathToAssets + 'skills.json', json, function (err) {
    if (err) {
        return console.log(err);
    }

    console.log("skill.json was generated and put into assets");
});
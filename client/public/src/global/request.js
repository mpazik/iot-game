Request = {};
Request.Server = (function () {

    function request(method, path, action) {
        return new Promise(function (resolve, reject) {
            const httpRequest = new XMLHttpRequest();
            httpRequest.onreadystatechange = function () {
                if (httpRequest.readyState === XMLHttpRequest.DONE) {
                    action(httpRequest, resolve, reject);
                }
            };
            httpRequest.open(method, Configuration.containerRestAddress + '/' + path);
            httpRequest.send();
        });
    }

    function get(path, action) {
        return request('get', path, action);
    }

    return {
        leaderboard: function () {
            return get('leaderboard', function (httpRequest, resolve, reject) {
                if (httpRequest.status === 200) {
                    resolve(JSON.parse(httpRequest.responseText));
                } else {
                    reject(httpRequest.responseText)
                }
            })
        },
        userLeaderboardResult: function (userId) {
            return get('leaderboard/user/' + userId, function (httpRequest, resolve, reject) {
                if (httpRequest.status === 200) {
                    resolve(JSON.parse(httpRequest.responseText));
                } else {
                    reject(httpRequest.responseText)
                }
            })

        }
    }
})();
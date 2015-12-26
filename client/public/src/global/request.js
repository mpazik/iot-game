Request = {};
Request.Server = (function () {

    return {
        leaderboard: function () {
            return new Promise(function (resolve, reject) {
                const httpRequest = new XMLHttpRequest();
                httpRequest.onreadystatechange = function () {
                    if (httpRequest.readyState === XMLHttpRequest.DONE) {
                        if (httpRequest.status === 200) {
                            const response = JSON.parse(httpRequest.responseText);
                            resolve(response);
                        } else {
                            reject(httpRequest.responseText)
                        }
                    }
                };
                httpRequest.open('GET', '/leaderboard');
                httpRequest.send();
            });
        }
    }
})();
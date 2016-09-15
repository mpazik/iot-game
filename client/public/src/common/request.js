define(['configuration'], (Configuration) => {
    return {
        request(method, path, data) {
            return new Promise(function (resolve, reject) {
                const httpRequest = new XMLHttpRequest();
                httpRequest.onreadystatechange = function () {
                    if (httpRequest.readyState === XMLHttpRequest.DONE) {
                        if (httpRequest.status >= 200 && httpRequest.status < 300) {
                            resolve(httpRequest.responseText ? JSON.parse(httpRequest.responseText) : undefined, httpRequest.status);
                        } else {
                            reject(JSON.parse(httpRequest.responseText).message, httpRequest.status)
                        }
                    }
                };
                httpRequest.open(method, Configuration.containerRestAddress + '/' + path);
                httpRequest.send(data ? JSON.stringify(data) : undefined);
            });
        },

        get(path) {
            return this.request('get', path);
        },

        post(path, data) {
            return this.request('post', path, data);
        }
    }
});

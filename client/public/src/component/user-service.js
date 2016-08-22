define(['../common/request'], (request) => {
    const reissueTokenKey = 'reissueToken';

    var userToken = null;

    function saveReissueToken(token) {
        localStorage.setItem(reissueTokenKey, token);
    }

    function removeReissueToken() {
        localStorage.removeItem(reissueTokenKey);
    }

    return {
        get userToken() {
            return userToken;
        },
        reissueUserToken () {
            return new Promise((resolve, reject) => {
                const reissueToken = localStorage.getItem(reissueTokenKey);
                if (reissueToken == null) {
                    reject();
                    return;
                }
                request.post('reissue', {token: reissueToken})
                    .then(response => {
                        userToken = response.loginToken;
                        resolve(userToken);
                    })
                    .catch(reject);
            })
        },
        login (nick, password) {
            return request.post('login', {nick, password}).then(response => {
                userToken = response.loginToken;
                saveReissueToken(response.reissueToken);
                return response.loginToken;
            });
        },
        logout () {
            userToken = null;
            removeReissueToken();
        }
    }
});

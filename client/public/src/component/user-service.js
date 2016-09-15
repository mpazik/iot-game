define(['configuration', '../common/request'], (Configuration, request) => {
    const reissueTokenKey = 'reissueToken';

    var userToken = null;

    function saveReissueToken(token) {
        localStorage.setItem(reissueTokenKey, token);
    }

    function removeReissueToken() {
        localStorage.removeItem(reissueTokenKey);
    }

    function clearUserToken() {
        userToken = null;
    }

    function login(nick, password) {
        return request.post('login', {nick, password}).then(response => {
            userToken = response.loginToken;
            saveReissueToken(response.reissueToken);
            return response.loginToken;
        });
    }

    function reissueUserToken() {
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
    }

    function loginUsingQueryParams() {
        return new Promise((resolve, reject) => {
            const searchParams = new URLSearchParams(window.location.search.slice(1));
            if (searchParams.get('nick') == null) {
                reject();
                return;
            }
            const nick = searchParams.get('nick');
            const password = searchParams.get('pwd') ? searchParams.get('pwd') : nick;
            login(nick, password)
                .then(resolve)
                .catch((error) => {
                    console.error(error);
                    reject(error);
                })
        });
    }

    return {
        get userToken() {
            return userToken;
        },
        tryLoginUsingClientData () {
            if (Configuration.devMode) {
                return loginUsingQueryParams().catch(reissueUserToken);
            } else {
                return reissueUserToken();
            }
        },
        logout () {
            clearUserToken();
            removeReissueToken();
        },
        clearUserToken
    }
});

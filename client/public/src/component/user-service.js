define(['../common/request'], (request) => {

    function saveReissueToken(token) {

    }

    return {
        login (nick, password) {
            return request.post('login', {nick, password}).then(response => {
                saveReissueToken(response.reissueToken);
                return response.loginToken;
            });
        }
    }
});

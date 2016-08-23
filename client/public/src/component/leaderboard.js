define(['../common/request'], (request) => {

    return {
        leaderboard () {
            return request.get('leaderboard');
        },
        userLeaderboardResult (userId) {
            return request.get('leaderboard/user/' + userId);
        }
    }
});

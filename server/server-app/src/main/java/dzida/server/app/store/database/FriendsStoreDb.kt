package dzida.server.app.store.database

import com.querydsl.sql.SQLExpressions
import dzida.server.app.database.ConnectionProvider
import dzida.server.app.friend.FriendServer
import dzida.server.app.friend.FriendsStore
import dzida.server.app.querydsl.QFriendship.friendship
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id

class FriendsStoreDb : FriendsStore {
    val connectionProvider: ConnectionProvider

    constructor(connectionProvider: ConnectionProvider) {
        this.connectionProvider = connectionProvider
    }

    override fun save(friendshipEvent: FriendServer.Friendship) = connectionProvider.withSqlFactory { sqlQueryFactory ->
        sqlQueryFactory.insert(friendship!!)
                .set(friendship.userId1, friendshipEvent.userId1.intValue)
                .set(friendship.userId2, friendshipEvent.userId2.intValue)
                .execute()
    }

    override fun getUserFriends(userId: Id<User>): Iterable<Id<User>> {
        return connectionProvider.withSqlFactory<List<Id<User>>> { sqlQueryFactory ->
            val sqlQuery1 = SQLExpressions.select(friendship.userId1).from(friendship).where(friendship.userId2.eq(userId.intValue))
            val sqlQuery2 = SQLExpressions.select(friendship.userId2).from(friendship).where(friendship.userId1.eq(userId.intValue))
            val query = sqlQueryFactory.select(friendship.userId1).union(sqlQuery1, sqlQuery2)
            val ids: List<Int> = query.fetch()
            ids.map({ Id<User>(it.toLong()) })
        }
    }
}

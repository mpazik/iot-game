package dzida.server.app.friend

import dzida.server.app.basic.entity.Id
import dzida.server.app.user.User


interface FriendsStore {
    fun save(friendshipEvent: FriendServer.Friendship)
    fun getUserFriends(userId: Id<User>): Iterable<Id<User>>
}
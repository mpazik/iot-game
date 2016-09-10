package dzida.server.app.friend

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id


interface FriendsStore {
    fun save(friendshipEvent: FriendServer.Friendship)
    fun getUserFriends(userId: Id<User>): Iterable<Id<User>>
}
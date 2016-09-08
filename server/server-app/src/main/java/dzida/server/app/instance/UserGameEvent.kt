package dzida.server.app.instance

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id
import dzida.server.core.event.CharacterEvent

data class UserGameEvent(var userId: Id<User>, var event: CharacterEvent)

package dzida.server.app.instance

import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.event.CharacterEvent
import dzida.server.app.user.User

interface UserMessage<out T> {
    val userId: Id<User>
    val message: T

    data class UserGameEvent(
            override val userId: Id<User>,
            override val message: CharacterEvent
    ) : UserMessage<CharacterEvent>

    data class UserCommand(
            override val userId: Id<User>,
            override val message: CharacterCommand
    ) : UserMessage<CharacterCommand>
}

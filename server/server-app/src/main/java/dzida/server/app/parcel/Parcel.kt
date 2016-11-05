package dzida.server.app.parcel

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id

data class Parcel(val id: Id<Parcel>, val owner: Id<User>, val x: Int, val y: Int)



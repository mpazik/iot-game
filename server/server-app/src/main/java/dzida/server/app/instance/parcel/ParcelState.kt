package dzida.server.app.instance.parcel

import dzida.server.app.parcel.ParcelChange

class ParcelState private constructor(
        val parcelChanges: List<ParcelChange>
) {
    constructor() : this(listOf())

    fun addChange(change: ParcelChange) = ParcelState(parcelChanges.plus(change))
}
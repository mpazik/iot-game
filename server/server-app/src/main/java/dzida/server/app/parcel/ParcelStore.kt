package dzida.server.app.parcel

interface ParcelStore {
    fun getParcelChanges(): List<ParcelChange>
    fun saveChange(change: ParcelChange)
}
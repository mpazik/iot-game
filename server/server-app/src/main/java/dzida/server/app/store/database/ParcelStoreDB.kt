package dzida.server.app.store.database

import dzida.server.app.database.ConnectionProvider
import dzida.server.app.parcel.ParcelChange
import dzida.server.app.parcel.ParcelStore
import dzida.server.app.querydsl.QParcelsEvent.parcelsEvent
import dzida.server.app.serialization.BasicJsonSerializer
import dzida.server.app.serialization.MessageSerializer

class ParcelStoreDB : ParcelStore {
    val connectionProvider: ConnectionProvider

    val eventSerializer: MessageSerializer = MessageSerializer.create(ParcelChange.classes)

    constructor(connectionProvider: ConnectionProvider) {
        this.connectionProvider = connectionProvider
    }

    override fun getParcelChanges(): List<ParcelChange> {
        return connectionProvider.withSqlFactory<List<ParcelChange>> { sqlQueryFactory ->
            val fetch = sqlQueryFactory
                    .select(parcelsEvent.type, parcelsEvent.data)
                    .from(parcelsEvent)
                    .orderBy(parcelsEvent.createdAt.asc())
                    .fetch()
            fetch.map({ tuple ->
                val type = tuple.get(parcelsEvent.type)!!
                val data = tuple.get(parcelsEvent.data)!!
                val messageClass = eventSerializer.getMessageClass(type)
                checkNotNull(messageClass, { "Achievement type <$type> does not exists. Wrong data in DB?" })

                eventSerializer.parseEvent(data, type) as ParcelChange
            })
        }
    }

    override fun saveChange(change: ParcelChange) {
        val type = eventSerializer.getMessageType(change)

        val data = BasicJsonSerializer.getSerializer().toJson(change)
        connectionProvider.withSqlFactory { sqlQueryFactory ->
            sqlQueryFactory.insert(parcelsEvent!!)
                    .set(parcelsEvent.data, data)
                    .set(parcelsEvent.type, type)
                    .execute()
        }
    }

}



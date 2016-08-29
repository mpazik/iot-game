package dzida.server.app.store.database;

import dzida.server.app.chat.ChatStore;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;

import static dzida.server.app.querydsl.QChatCommand.chatCommand;

public class ChatStoreDb implements ChatStore {

    private final ConnectionProvider connectionProvider;

    public ChatStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void saveUserCommand(Id<User> userId, String command) {
        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(chatCommand)
                    .set(chatCommand.userId, userId.getIntValue())
                    .set(chatCommand.command, command)
                    .execute();
        });
    }

    @Override
    public void saveSystemCommand(String command) {
        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(chatCommand)
                    .set(chatCommand.command, command)
                    .execute();
        });
    }
}

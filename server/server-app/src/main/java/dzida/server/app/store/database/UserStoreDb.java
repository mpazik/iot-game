package dzida.server.app.store.database;

import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.user.User;
import dzida.server.app.user.UserStore;

import java.util.Optional;

import static dzida.server.app.querydsl.QUserPassword.userPassword;
import static dzida.server.app.querydsl.QUserRegistration.userRegistration;

public class UserStoreDb implements UserStore {
    private final ConnectionProvider connectionProvider;

    public UserStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Outcome<Id<User>> registerNewUser(String nick, String email, String password) {
        return connectionProvider.withSqlFactorySafe(sqlQueryFactory -> {
            Integer userId = sqlQueryFactory.insert(userRegistration)
                    .set(userRegistration.nick, nick)
                    .set(userRegistration.email, email)
                    .executeWithKey(userRegistration.id);
            assert userId != null;
            sqlQueryFactory.insert(userPassword)
                    .set(userPassword.userId, userId)
                    .set(userPassword.password, password)
                    .execute();
            return new Id<User>(userId);
        });
    }

    @Override
    public Optional<Id<User>> getUserIdByNick(String nick) {
        return connectionProvider.withSqlFactory(sqlQueryFactory -> {
            Integer id = sqlQueryFactory.select(userRegistration.id).from(userRegistration)
                    .where(userRegistration.nick.eq(nick))
                    .fetchOne();
            return Optional.ofNullable(id).map(Id::new);
        });
    }

    @Override
    public String getUserPassword(Id<User> userId) {
        return connectionProvider.withSqlFactory(sqlQueryFactory -> {
            return sqlQueryFactory.select(userPassword.password).from(userPassword)
                    .where(userPassword.userId.eq(userId.getIntValue()))
                    .orderBy(userPassword.createdAt.desc())
                    .fetchFirst();
        });
    }

    @Override
    public String getUserNick(Id<User> userId) {
        return connectionProvider.withSqlFactory(sqlQueryFactory -> {
            return sqlQueryFactory.select(userRegistration.nick).from(userRegistration)
                    .where(userRegistration.id.eq(userId.getIntValue()))
                    .fetchOne();
        });
    }
}

package dzida.server.app.database;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import dzida.server.app.basic.Outcome;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

// Consumer is used so in future, provider can easily use connection pool and stop connection after doing query.
@SuppressWarnings("unused")
public class ConnectionProvider {

    private final Configuration queryDslConfiguration;
    private final Connection connection;

    ConnectionProvider(Connection connection) {
        this.connection = connection;
        SQLTemplates templates = new PostgreSQLTemplates();
        queryDslConfiguration = new Configuration(templates);
    }

    public void withConnection(Consumer<Connection> connectionConsumer) {
        connectionConsumer.accept(connection);
    }

    public <T> T withConnection(Function<Connection, T> connectionConsumer) {
        return connectionConsumer.apply(connection);
    }

    public void withSqlFactory(Consumer<SQLQueryFactory> sqlQueryFactoryConsumer) {
        SQLQueryFactory queryFactory = new SQLQueryFactory(queryDslConfiguration, () -> connection);
        sqlQueryFactoryConsumer.accept(queryFactory);
    }

    public <T> T withSqlFactory(Function<SQLQueryFactory, T> sqlQueryFactoryConsumer) {
        SQLQueryFactory queryFactory = new SQLQueryFactory(queryDslConfiguration, () -> connection);
        return sqlQueryFactoryConsumer.apply(queryFactory);
    }

    public <T> Outcome<T> withSqlFactorySafe(Function<SQLQueryFactory, T> sqlQueryFactoryConsumer) {
        SQLQueryFactory queryFactory = new SQLQueryFactory(queryDslConfiguration, () -> connection);
        try {
            return Outcome.ok(sqlQueryFactoryConsumer.apply(queryFactory));
        } catch (RuntimeException e) {
            return Outcome.error(e.getCause().getMessage());
        }
    }
}

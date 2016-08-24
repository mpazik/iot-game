package dzida.server.app.user;

import dzida.server.core.basic.entity.Id;

import java.util.Optional;

public interface UserStore {
    Id<User> registerNewUser(String nick, String email, String password);

    Optional<Id<User>> getUserIdByNick(String nick);

    String getUserPassword(Id<User> userId);

    String getUserNick(Id<User> userId);
}

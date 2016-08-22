package dzida.server.app.user;

import dzida.server.core.basic.entity.Id;

public class LoginToken {
    final Id<User> userId;
    final String nick;

    public LoginToken(Id<User> userId, String nick) {
        this.userId = userId;
        this.nick = nick;
    }
}

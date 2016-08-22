package dzida.server.app.user;

import dzida.server.core.basic.entity.Id;

public class LoginToken {
    public final Id<User> userId;
    public final String nick;

    public LoginToken(Id<User> userId, String nick) {
        this.userId = userId;
        this.nick = nick;
    }
}

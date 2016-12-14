package dzida.server.app.chat;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.user.User;

public interface ChatStore {
    void saveUserCommand(Id<User> userId, String command);

    void saveSystemCommand(String command);
}

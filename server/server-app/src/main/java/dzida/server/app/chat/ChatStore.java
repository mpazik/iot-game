package dzida.server.app.chat;

import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;

public interface ChatStore {
    void saveUserCommand(Id<User> userId, String command);

    void saveSystemCommand(String command);
}

package dzida.server.app.command;

import dzida.server.app.instance.command.InstanceCommand;

public class SendMessageCommand implements InstanceCommand {
    public final String message;

    public SendMessageCommand(String message) {
        this.message = message;
    }
}

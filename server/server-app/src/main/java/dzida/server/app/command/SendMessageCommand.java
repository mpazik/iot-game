package dzida.server.app.command;

public class SendMessageCommand implements InstanceCommand {
    public final String message;

    public SendMessageCommand(String message) {
        this.message = message;
    }
}

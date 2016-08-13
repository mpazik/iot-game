package dzida.server.app.command;

public class SendMessageCommand implements Command {
    public final String message;

    public SendMessageCommand(String message) {
        this.message = message;
    }
}

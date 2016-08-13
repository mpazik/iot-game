package dzida.server.app.command;

public class JoinBattleCommand implements Command {
    public final String map;
    public final int difficultyLevel;

    public JoinBattleCommand(String map, int difficultyLevel) {
        this.map = map;
        this.difficultyLevel = difficultyLevel;
    }
}

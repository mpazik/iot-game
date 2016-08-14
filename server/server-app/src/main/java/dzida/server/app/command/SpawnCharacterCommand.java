package dzida.server.app.command;

import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.model.Character;

import javax.annotation.Nullable;

public class SpawnCharacterCommand implements InstanceCommand {
    public final Character character;

    @Nullable
    public final Point spawnPoint;

    public SpawnCharacterCommand(Character character) {
        this.character = character;
        this.spawnPoint = null;
    }

    public SpawnCharacterCommand(Character character, @Nullable Point spawnPoint) {
        this.character = character;
        this.spawnPoint = spawnPoint;
    }
}

package dzida.server.core.character.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.entity.Command;

public class SpawnCharacterCommand implements Command{
    public final Id<Character> characterId;

    public SpawnCharacterCommand(Id<Character> characterId) {
        this.characterId = characterId;
    }
}

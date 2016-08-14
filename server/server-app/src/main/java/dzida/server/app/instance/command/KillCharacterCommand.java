package dzida.server.app.instance.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public class KillCharacterCommand implements InstanceCommand {
    public final Id<Character> characterId;

    public KillCharacterCommand(Id<Character> characterId) {
        this.characterId = characterId;
    }
}

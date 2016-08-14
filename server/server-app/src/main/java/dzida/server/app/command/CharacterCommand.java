package dzida.server.app.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public interface CharacterCommand extends Command {
    InstanceCommand getInstanceCommand(Id<Character> characterId);
}

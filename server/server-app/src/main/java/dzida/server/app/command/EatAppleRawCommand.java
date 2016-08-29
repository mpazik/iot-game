package dzida.server.app.command;

import dzida.server.app.instance.command.EatAppleCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public class EatAppleRawCommand implements CharacterCommand {
    @Override
    public InstanceCommand getInstanceCommand(Id<Character> characterId) {
        return new EatAppleCommand(characterId);
    }
}

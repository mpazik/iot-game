package dzida.server.app.instance.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public interface InstanceCommand {

    class EatAppleCommand implements InstanceCommand {

        public final Id<Character> characterId;

        public EatAppleCommand(Id<Character> characterId) {
            this.characterId = characterId;
        }

    }

    class EatRottenAppleCommand implements InstanceCommand {
        public final Id<Character> characterId;

        public EatRottenAppleCommand(Id<Character> characterId) {
            this.characterId = characterId;
        }
    }
}

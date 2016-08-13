package dzida.server.app;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dzida.server.app.command.Command;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class BackdoorCommandResolver {
    public static BackdoorCommandResolver NoOpResolver = new BackdoorCommandResolver() {
        @Override
        public List<GameEvent> resolveCommand(Id<Character> characterId, BackdoorCommand command, Consumer<GameEvent> send) {
            return Collections.emptyList();
        }
    };

    private interface Commands {
        int KillCharacter = 0;
    }

    public List<GameEvent> resolveCommand(Id<Character> characterId, BackdoorCommand command, Consumer<GameEvent> send) {
        return dispatchMessage(characterId, command.type, command.data, send);
    }

    private List<GameEvent> dispatchMessage(Id<Character> characterId, int type, JsonElement data, Consumer<GameEvent> send) {
        switch (type) {
            case Commands.KillCharacter:
                return singletonList(new CharacterDied(characterId));
        }
        return emptyList();
    }

    public static final class BackdoorCommand implements Command {
        int type;
        JsonObject data;

        public BackdoorCommand(int type, JsonObject data) {
            this.type = type;
            this.data = data;
        }
    }
}

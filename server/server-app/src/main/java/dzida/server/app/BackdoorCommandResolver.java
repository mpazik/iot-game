package dzida.server.app;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.event.GameEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class BackdoorCommandResolver {
    public static BackdoorCommandResolver NoOpResolver = new BackdoorCommandResolver(null) {
        @Override
        public List<GameEvent> resolveCommand(CharacterId characterId, JsonElement command, Consumer<GameEvent> send) {
            return Collections.emptyList();
        }
    };

    private final Gson serializer;

    public BackdoorCommandResolver(Gson serializer) {
        this.serializer = serializer;
    }

    private interface Commands {
        int KillCharacter = 0;
    }

    public List<GameEvent> resolveCommand(CharacterId characterId, JsonElement payload, Consumer<GameEvent> send) {
        BackdoorCommand command = serializer.fromJson(payload, BackdoorCommand.class);
        return dispatchMessage(characterId, command.type, command.data, send);
    }

    private List<GameEvent> dispatchMessage(CharacterId characterId, int type, JsonElement data, Consumer<GameEvent> send) {
        switch (type) {
            case Commands.KillCharacter:
                return singletonList(new CharacterDied(characterId));
        }
        return emptyList();
    }

    public static final class BackdoorCommand {
        int type;
        JsonObject data;

        public BackdoorCommand(int type, JsonObject data) {
            this.type = type;
            this.data = data;
        }
    }
}

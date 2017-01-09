package dzida.server.app.instance.command;

import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.CharacterDied;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;

import java.util.List;

import static java.util.Collections.singletonList;

public class KillCharacterCommand implements InstanceCommand {
    public final Id<Character> characterId;

    public KillCharacterCommand(Id<Character> characterId) {
        this.characterId = characterId;
    }

    @Override
    public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
        if (!state.getCharacter().isCharacterLive(characterId)) {
            return Outcome.error("Character that is already died can not be killed.");
        }
        return Outcome.ok(singletonList(new CharacterDied(characterId)));
    }
}

package dzida.server.core.character;

import com.google.common.collect.FluentIterable;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

class CharacterServiceImpl implements CharacterService {

    public static final String Key = "character";
    private final Map<CharacterId, Character> state = new HashMap<>();

    @Override
    public List<Character> getState() {
        return state.values().stream().collect(Collectors.toList());
    }

    @Override
    public String getKey() {
        return Key;
    }

    @Override
    public boolean isCharacterEnemyFor(CharacterId character1, CharacterId character2) {
        return state.get(character1).getType() != state.get(character2).getType();
    }

    @Override
    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            Character character = event.getCharacter();
            state.put(character.getId(), character);
        }).is(CharacterDied.class).then(event -> state.remove(event.getCharacterId()));
    }

    @Override
    public <T extends Character> List<T> getCharactersOfType(Class<T> clazz) {
        return FluentIterable.from(state.values())
                .filter(clazz)
                .toList();
    }

    @Override
    public int getCharacterType(CharacterId characterId) {
        return state.get(characterId).getType();
    }

    @Override
    public Optional<PlayerCharacter> getPlayerCharacter(CharacterId characterId) {
        Character character = state.get(characterId);
        if (character.getType() == Character.Player) {
            return Optional.of((PlayerCharacter) character);
        }
        return Optional.empty();
    }

    @Override
    public boolean isCharacterLive(CharacterId characterId) {
        return state.containsKey(characterId);
    }
}

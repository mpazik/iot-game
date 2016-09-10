package dzida.server.core.character;

import com.google.common.collect.FluentIterable;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

class CharacterServiceImpl implements CharacterService {

    public static final String Key = "character";
    private final Map<Id<Character>, Character> state = new HashMap<>();

    @Override
    public List<Character> getState() {
        return state.values().stream().collect(Collectors.toList());
    }

    @Override
    public String getKey() {
        return Key;
    }

    @Override
    public boolean isCharacterEnemyFor(Id<Character> character1, Id<Character> character2) {
        return state.get(character1).getType() != state.get(character2).getType();
    }

    @Override
    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            Character character = event.character;
            state.put(character.getId(), character);
        }).is(CharacterDied.class).then(event -> state.remove(event.characterId));
    }

    @Override
    public <T extends Character> List<T> getCharactersOfType(Class<T> clazz) {
        //noinspection Guava
        return FluentIterable.from(state.values())
                .filter(clazz)
                .toList();
    }

    @Override
    public boolean isCharacterLive(Id<Character> characterId) {
        return state.containsKey(characterId);
    }

    @Override
    public Character getCharacter(Id<Character> characterId) {
        return state.get(characterId);
    }
}

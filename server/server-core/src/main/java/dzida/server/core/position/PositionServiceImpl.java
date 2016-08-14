package dzida.server.core.position;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.time.TimeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

final class PositionServiceImpl implements PositionService {
    public static final String Key = "move";

    private final Map<Id<Character>, Move> state = new HashMap<>();

    private final TimeService timeService;
    private final PositionStore positionStore;

    public PositionServiceImpl(PositionStore positionStore, TimeService timeService) {
        this.positionStore = positionStore;
        this.timeService = timeService;
    }

    @Override
    public List<CharacterMoved> getState() {
        return state.entrySet().stream()
                .map(entry -> new CharacterMoved(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public String getKey() {
        return Key;
    }

    @Override
    public boolean areCharactersInDistance(Id<Character> character1, Id<Character> character2, double distance, long time) {
        Point char1Pos = state.get(character1).getPositionAtTime(time);
        Point char2Pos = state.get(character2).getPositionAtTime(time);
        return char1Pos.isInRange(char2Pos, distance);
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            Id<Character> characterId = event.character.getId();
            state.put(characterId, event.move);
        }).is(CharacterDied.class).then(
                event -> state.remove(event.characterId)
        ).is(CharacterMoved.class).then(
                event -> state.put(event.characterId, event.move)
        );
    }

    @Override
    public Move getMove(Id<Character> characterId) {
        return state.get(characterId);
    }

    @Override
    public Point getPosition(Id<Character> characterId, long currentMillis) {
        return state.get(characterId).getPositionAtTime(currentMillis);
    }

    @Override
    public boolean isStanding(Id<Character> characterId, long currentMillis) {
        return state.get(characterId).isStanding(currentMillis);
    }

    @Override
    public Move getInitialMove(Id<Character> characterId, Point spawnPoint) {
        if (spawnPoint != null) {
            positionStore.setPosition(characterId, spawnPoint);
        }
        return Move.of(timeService.getCurrentMillis(), PlayerSpeed, positionStore.getCharacterPosition(characterId));
    }
}

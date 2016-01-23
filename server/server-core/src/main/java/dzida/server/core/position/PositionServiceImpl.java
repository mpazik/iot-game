package dzida.server.core.position;

import dzida.server.core.character.CharacterId;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
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

    private final Map<CharacterId, Move> state = new HashMap<>();

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
    public boolean areCharactersInDistance(CharacterId character1, CharacterId character2, double distance, long time) {
        Point char1Pos = state.get(character1).getPositionAtTime(time);
        Point char2Pos = state.get(character2).getPositionAtTime(time);
        return char1Pos.isInRange(char2Pos, distance);
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            CharacterId characterId = event.getCharacter().getId();
            state.put(characterId, event.getMove());
        }).is(CharacterDied.class).then(
                event -> state.remove(event.getCharacterId())
        ).is(CharacterMoved.class).then(
                event -> state.put(event.getCharacterId(), event.getMove())
        );
    }

    @Override
    public Move getMove(CharacterId characterId) {
        return state.get(characterId);
    }

    @Override
    public Point getPosition(CharacterId characterId, long currentMillis) {
        return state.get(characterId).getPositionAtTime(currentMillis);
    }

    @Override
    public boolean isStanding(CharacterId characterId, long currentMillis) {
        return state.get(characterId).isStanding(currentMillis);
    }

    @Override
    public Move getInitialMove(CharacterId characterId) {
        return Move.of(timeService.getCurrentMillis(), PlayerSpeed, positionStore.getCharacterPosition(characterId));
    }
}

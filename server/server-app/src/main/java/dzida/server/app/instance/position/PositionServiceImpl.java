package dzida.server.app.instance.position;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.event.CharacterDied;
import dzida.server.app.instance.character.event.CharacterSpawned;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.event.CharacterMoved;
import dzida.server.app.instance.world.event.WorldObjectCreated;
import dzida.server.app.instance.world.event.WorldObjectRemoved;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;
import dzida.server.app.instance.world.object.WorldObjectStore;
import dzida.server.app.instance.world.pathfinding.CollisionMap;
import dzida.server.app.instance.world.pathfinding.CollisionMapFactory;
import dzida.server.app.instance.world.pathfinding.PathFinder;
import dzida.server.app.time.TimeService;

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
    private final WorldObjectStore worldObjectStore;
    private final CollisionMapFactory collisionMapFactory;
    private final BitMap.ImmutableBitMap.Builder collisionBitMapBuilder;
    private PathFinder pathFinder;

    public PositionServiceImpl(PositionStore positionStore, TimeService timeService, WorldObjectStore worldObjectStore, BitMap worldCollisionBitMap) {
        this.positionStore = positionStore;
        this.timeService = timeService;
        this.worldObjectStore = worldObjectStore;
        this.collisionMapFactory = new CollisionMapFactory(5);
        collisionBitMapBuilder = BitMap.ImmutableBitMap.builder(worldCollisionBitMap);
        updatePathFinder();
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
        ).is(WorldObjectCreated.class).then(
                event -> {
                    WorldObject worldObject = event.worldObject.getData();
                    setWorldObjectCollsion(worldObject, true);
                }
        ).is(WorldObjectRemoved.class).then(
                event -> {
                    WorldObject worldObject = event.worldObject.getData();
                    setWorldObjectCollsion(worldObject, false);
                }
        );
    }

    private void setWorldObjectCollsion(WorldObject worldObject, boolean value) {
        WorldObjectKind worldObjectKind = worldObjectStore.getWorldObjectKind(worldObject.getKind());
        if (!worldObjectKind.isCollidable()) {
            return;
        }
        WorldObjectKind.GroundLayer groundLayer = worldObjectKind.getGroundLayer();
        if (groundLayer != null) {
            int startX = worldObject.getX() + groundLayer.getOffsetX();
            int startY = worldObject.getY() + groundLayer.getOffsetY();
            setRectangleCollision(startX, groundLayer.getWidth(), startY, groundLayer.getHeight(), value);
        } else {
            setRectangleCollision(worldObject.getX(), worldObjectKind.getWidth(), worldObject.getY(), worldObjectKind.getHeight(), value);
        }
        updatePathFinder();
    }

    private void setRectangleCollision(int startX, int width, int startY, int height, boolean value) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                collisionBitMapBuilder.set(x, y, value);
            }
        }
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
    public List<Point> findPathToDestination(Id<Character> characterId, Point destination) {
        Move move = getMove(characterId);
        Point currentPosition = move.getPositionAtTime(timeService.getCurrentMillis());
        return pathFinder.findPathToDestination(currentPosition, destination);
    }

    @Override
    public Move getInitialMove(Id<Character> characterId, Point spawnPoint) {
        if (spawnPoint != null) {
            positionStore.setPosition(characterId, spawnPoint);
        }
        return Move.of(timeService.getCurrentMillis(), PlayerSpeed, positionStore.getCharacterPosition(characterId));
    }

    private void updatePathFinder() {
        CollisionMap collisionMap = collisionMapFactory.createCollisionMap(collisionBitMapBuilder.build());
        pathFinder = new PathFinder(collisionMap);
    }
}

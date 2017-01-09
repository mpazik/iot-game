package dzida.server.app.instance;

import com.google.common.collect.ImmutableList;
import dzida.server.app.Configuration;
import dzida.server.app.Scheduler;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.Publisher;
import dzida.server.app.basic.Result;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.entity.Key;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.CharacterState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.parcel.ParcelState;
import dzida.server.app.instance.position.PositionState;
import dzida.server.app.instance.skill.SkillSate;
import dzida.server.app.instance.world.WorldObjectCreated;
import dzida.server.app.instance.world.WorldState;
import dzida.server.app.instance.world.map.WorldMap;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;
import dzida.server.app.instance.world.pathfinding.CollisionBitMap;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.parcel.ParcelChange;
import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.time.TimeService;
import dzida.server.app.timesync.TimeServiceImpl;
import dzida.server.app.user.User;

import javax.annotation.Nonnull;
import javax.ws.rs.NotSupportedException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Instance {

    private final Publisher<GameEvent> eventPublisher = new Publisher<>();

    private final String instanceKey;
    private final TimeService timeService;
    private final GameDefinitions gameDefinitions;
    private final GameLogic gameLogic;

    private GameState gameState;

    public Instance(String instanceKey, Scenario scenario, Scheduler scheduler, GameDefinitions gameDefinitions) {
        this.instanceKey = instanceKey;
        this.gameDefinitions = gameDefinitions;

        Key<WorldMap> worldMapKey = scenario.getWorldMapKey();
        StaticDataLoader staticDataLoader = new StaticDataLoader();
        timeService = new TimeServiceImpl();

        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader), timeService);


        WorldMap worldMap = worldMapStore.getMap(worldMapKey);

        BitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));

        gameState = new GameState(new CharacterState(), new WorldState(worldMap), new PositionState(collisionBitMap), new SkillSate(), new ParcelState());

        this.gameLogic = new GameLogic(scheduler, this);

        updateState(createSpawningParcel(worldMap));
        updateState(initGameObjects(worldMapKey, worldMapStore));
    }

    private ParcelChange.ParcelClaimed createSpawningParcel(WorldMap worldMap) {
        Point spawnPoint = worldMap.getSpawnPoint();
        int spawningParcelX = (int) (spawnPoint.getX()) / Configuration.ParcelSize;
        int spawningParcelY = (int) (spawnPoint.getY()) / Configuration.ParcelSize;
        return new ParcelChange.ParcelClaimed(spawningParcelX, spawningParcelY, new Id<>(0), "Kingdom", "Common land");
    }

    private List<GameEvent> initGameObjects(Key<WorldMap> worldMapKey, WorldMapStoreHttp worldMapStoreHttp) {
        WorldState world = gameState.getWorld();
        if (!world.isEmpty()) {
            return ImmutableList.of();
        }
        List<WorldObject> initialMapObjects = worldMapStoreHttp.getInitialMapObjects(worldMapKey);
        return initialMapObjects.stream()
                .map(worldObject -> {
                    WorldObjectKind worldObjectKind = gameDefinitions.getObjectKind(worldObject.getKind());
                    int y = worldObject.getY() - worldObjectKind.getHeight() + 1;
                    GeneralEntity<WorldObject> objectEntity = world.createWorldObject(worldObject.getKind(), worldObject.getX(), y, timeService.getCurrentMillis());
                    return new WorldObjectCreated(objectEntity);
                })
                .collect(Collectors.toList());
    }

    public void start() {
        gameLogic.start();
    }

    public GameState getState() {
        return gameState;
    }

    public void subscribeChange(Consumer<GameEvent> subscriber) {
        eventPublisher.subscribe(subscriber);
    }

    public Result handleCommand(InstanceCommand command) {
        // temporary validation done here. Eventually this will be asynchronous so it couldn't return any response.
        // in that case validation would have to be performed on InstanceServer on a copy of the state that may be a bit outdated.
        Outcome<List<GameEvent>> optionalGameEvents = command.process(gameState, gameDefinitions, timeService.getCurrentMillis());
        optionalGameEvents.toOptional().ifPresent(this::updateState);
        return optionalGameEvents.toResult();
    }

    private void updateState(List<GameEvent> gameEvents) {
        gameEvents.forEach(this::updateState);
    }

    void updateState(GameEvent gameEvent) {
        gameState = gameEvent.updateState(gameState, gameDefinitions);
        eventPublisher.notify(gameEvent);
    }

    public String getKey() {
        return instanceKey;
    }

    public GameDefinitions getGameDefinitions() {
        return gameDefinitions;
    }

    public static final class UserCharacter implements GameEvent {
        public final Id<Character> characterId;
        public final Id<User> userId;
        public final String userNick;

        public UserCharacter(Id<Character> characterId, Id<User> userId, String userNick) {
            this.characterId = characterId;
            this.userId = userId;
            this.userNick = userNick;
        }

        @Nonnull
        @Override
        public GameState updateState(@Nonnull GameState state, GameDefinitions definitions) {
            throw new NotSupportedException();
        }
    }
}


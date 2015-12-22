package dzida.server.app;

import com.google.common.collect.ImmutableMap;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.basic.Publisher;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.PlayerId;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.SkillService;
import dzida.server.core.world.WorldService;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameEventDispatcher {
    private final PositionService positionService;
    private final Publisher<GameEvent> eventPublisher = new Publisher<>();
    private final Publisher<GameEvent> eventPublisherBeforeChanges = new Publisher<>();
    private final Map<CharacterId, Consumer<GameEvent>> listeners = new HashMap<>();
    private final CharacterService characterService;
    private final WorldService worldService;
    private final SkillService skillService;
    private final Scenario scenario;

    public GameEventDispatcher(
            PositionService positionService,
            CharacterService characterService,
            WorldService worldService,
            SkillService skillService,
            Scenario scenario) {
        this.positionService = positionService;
        this.characterService = characterService;
        this.worldService = worldService;
        this.skillService = skillService;
        this.scenario = scenario;
    }

    public void registerCharacter(Character character, Consumer<GameEvent> send) {
        Consumer<GameEvent> listener = send::accept;
        eventPublisher.subscribe(listener);
        listeners.put(character.getId(), listener);
    }

    // I do not think that this should be here.
    public void sendInitialPacket(CharacterId characterId, PlayerId playerId) {
        listeners.get(characterId).accept(new InitialMessage(characterId, playerId, getState(), scenario));
    }

    private Map<String, Object> getState() {
        return ImmutableMap.of(
                positionService.getKey(), positionService.getState(),
                characterService.getKey(), characterService.getState(),
                worldService.getKey(), worldService.getState(),
                skillService.getKey(), skillService.getState()
        );
    }

    public void unregisterCharacter(CharacterId characterId) {
        eventPublisher.unsubscribe(listeners.get(characterId));
        listeners.remove(characterId);
    }

    public void dispatchEvent(GameEvent gameEvent) {
        eventPublisherBeforeChanges.notify(gameEvent);
        characterService.processEvent(gameEvent);
        positionService.processEvent(gameEvent);
        skillService.processEvent(gameEvent);
        eventPublisher.notify(gameEvent);
    }

    public void dispatchEvents(List<GameEvent> gameEvents) {
        gameEvents.stream().forEach(this::dispatchEvent);
    }

    public Publisher<GameEvent> getEventPublisherBeforeChanges() {
        return eventPublisherBeforeChanges;
    }

    public Publisher<GameEvent> getEventPublisher() {
        return eventPublisher;
    }

    @Value
    public static final class InitialMessage implements GameEvent {
        CharacterId characterId;
        PlayerId playerId;
        Map<String, Object> state;
        Scenario scenario;

        @Override
        public int getId() {
            return GameEvent.InitialData;
        }
    }
}

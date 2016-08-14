package dzida.server.app.instance;

import com.google.common.collect.ImmutableMap;
import dzida.server.core.basic.Publisher;
import dzida.server.core.character.CharacterService;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.SkillService;
import dzida.server.core.world.map.WorldMapService;
import dzida.server.core.world.object.WorldObjectService;

import java.util.List;
import java.util.Map;

public class InstanceStateManager {
    private final PositionService positionService;
    private final Publisher<GameEvent> eventPublisher = new Publisher<>();
    private final Publisher<GameEvent> eventPublisherBeforeChanges = new Publisher<>();

    private final CharacterService characterService;
    private final WorldMapService worldMapService;
    private final SkillService skillService;
    private final WorldObjectService worldObjectService;

    public InstanceStateManager(
            PositionService positionService,
            CharacterService characterService,
            WorldMapService worldMapService,
            SkillService skillService,
            WorldObjectService worldObjectService) {
        this.positionService = positionService;
        this.characterService = characterService;
        this.worldMapService = worldMapService;
        this.skillService = skillService;
        this.worldObjectService = worldObjectService;
    }

    public Map<String, Object> getState() {
        return ImmutableMap.of(
                positionService.getKey(), positionService.getState(),
                characterService.getKey(), characterService.getState(),
                worldMapService.getKey(), worldMapService.getState(),
                skillService.getKey(), skillService.getState(),
                worldObjectService.getKey(), worldObjectService.getState()
        );
    }

    public void dispatchEvent(GameEvent gameEvent) {
        eventPublisherBeforeChanges.notify(gameEvent);
        characterService.processEvent(gameEvent);
        positionService.processEvent(gameEvent);
        skillService.processEvent(gameEvent);
        worldObjectService.processEvent(gameEvent);
        eventPublisher.notify(gameEvent);
    }

    public void updateState(List<GameEvent> gameEvents) {
        gameEvents.forEach(this::dispatchEvent);
    }

    public Publisher<GameEvent> getEventPublisherBeforeChanges() {
        return eventPublisherBeforeChanges;
    }

    public Publisher<GameEvent> getEventPublisher() {
        return eventPublisher;
    }

    public PositionService getPositionService() {
        return positionService;
    }

    public CharacterService getCharacterService() {
        return characterService;
    }

    public WorldMapService getWorldMapService() {
        return worldMapService;
    }

    public SkillService getSkillService() {
        return skillService;
    }

    public WorldObjectService getWorldObjectService() {
        return worldObjectService;
    }
}

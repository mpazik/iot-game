package dzida.server.app.instance;

import com.google.common.collect.ImmutableMap;
import dzida.server.app.basic.Publisher;
import dzida.server.app.instance.character.CharacterService;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.PositionService;
import dzida.server.app.instance.skill.SkillService;
import dzida.server.app.instance.world.map.WorldMapService;
import dzida.server.app.instance.world.object.WorldObjectService;
import dzida.server.app.parcel.ParcelService;

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
    private final ParcelService parcelService;

    public InstanceStateManager(
            PositionService positionService,
            CharacterService characterService,
            WorldMapService worldMapService,
            SkillService skillService,
            WorldObjectService worldObjectService,
            ParcelService parcelService) {
        this.positionService = positionService;
        this.characterService = characterService;
        this.worldMapService = worldMapService;
        this.skillService = skillService;
        this.worldObjectService = worldObjectService;
        this.parcelService = parcelService;
    }

    public Map<String, Object> getState() {
        return ImmutableMap.<String, Object>builder()
                .put(positionService.getKey(), positionService.getState())
                .put(characterService.getKey(), characterService.getState())
                .put(worldMapService.getKey(), worldMapService.getState())
                .put(skillService.getKey(), skillService.getState())
                .put(worldObjectService.getKey(), worldObjectService.getState())
                .put(parcelService.getKey(), parcelService.getState())
                .build();
    }

    public void dispatchEvent(GameEvent gameEvent) {
        eventPublisherBeforeChanges.notify(gameEvent);
        characterService.processEvent(gameEvent);
        positionService.processEvent(gameEvent);
        skillService.processEvent(gameEvent);
        worldObjectService.processEvent(gameEvent);
        parcelService.processEvent(gameEvent);
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

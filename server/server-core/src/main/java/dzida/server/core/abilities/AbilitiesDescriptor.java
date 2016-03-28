package dzida.server.core.abilities;

import dzida.server.core.character.CharacterService;
import dzida.server.core.entity.*;
import dzida.server.core.skill.SkillService;
import dzida.server.core.time.TimeService;

public class AbilitiesDescriptor implements EntityDescriptor<Abilities> {
    private final EntityType<Abilities> entityType;
    private final AbilitiesCommandProcessor commandProcessor;

    public AbilitiesDescriptor(
            EntityType<Abilities> entityType,
            GeneralStateStore generalStateStore,
            CharacterService characterService,
            SkillService skillService,
            TimeService timeService) {
        this.entityType = entityType;
        StateRepository<Abilities> stateRepository = new StateRepository<>(generalStateStore, entityType);
        this.commandProcessor = new AbilitiesCommandProcessor(characterService, skillService, stateRepository, timeService);
    }

    @Override
    public CommandProcessor<Abilities> getCommandProcessor() {
        return commandProcessor;
    }

    @Override
    public EntityType<Abilities> getEntityType() {
        return entityType;
    }
}

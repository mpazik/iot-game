package dzida.server.app.scenario;

import dzida.server.app.GameEventDispatcher;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.Npc;
import dzida.server.core.character.CharacterId;
import dzida.server.core.player.PlayerId;
import dzida.server.core.Scheduler;
import dzida.server.core.scenario.SurvivalScenarioFactory.SurvivalScenario;

public class SurvivalScenarioLogic implements ScenarioLogic {

    private final NpcScenarioLogic npcScenarioLogic;
    private final Survival survival;
    private final SurvivalScenario survivalScenario;

    public SurvivalScenarioLogic(Scheduler scheduler, GameEventDispatcher gameEventDispatcher, NpcScenarioLogic npcScenarioLogic, Survival survival, SurvivalScenario survivalScenario) {
        this.npcScenarioLogic = npcScenarioLogic;
        this.survival = survival;
        this.survivalScenario = survivalScenario;
    }

    @Override
    public void handlePlayerDead(CharacterId characterId, PlayerId playerId) {

    }

    @Override
    public void start() {
        survival.getSpawns().stream().forEach(spawn -> npcScenarioLogic.addNpc(spawn.getPosition(), Npc.Fighter));
    }


}

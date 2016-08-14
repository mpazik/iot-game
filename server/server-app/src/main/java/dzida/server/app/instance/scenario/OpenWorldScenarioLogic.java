package dzida.server.app.instance.scenario;

import dzida.server.app.instance.command.SpawnCharacterCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

public class OpenWorldScenarioLogic implements ScenarioLogic {
    private static final int SPAWN_TIME = 4000;

    private final PlayerService playerService;
    private final InstanceCommandScheduler instanceCommandScheduler;

    public OpenWorldScenarioLogic(
            PlayerService playerService,
            InstanceCommandScheduler instanceCommandScheduler) {
        this.playerService = playerService;
        this.instanceCommandScheduler = instanceCommandScheduler;
    }

    @Override
    public void handlePlayerDead(Id<Character> characterId, Id<Player> playerId) {
        String playerNick = playerService.getPlayer(playerId).getData().getNick();
        respawnPlayer(characterId, playerNick, playerId);
    }

    @Override
    public void start() {

    }

    private void respawnPlayer(Id<Character> characterId, String nick, Id<Player> playerId) {
        PlayerCharacter newPlayerCharacter = new PlayerCharacter(characterId, nick, playerId);
//        long respawnTime = Instant.now().plusMillis(SPAWN_TIME).toEpochMilli();
        instanceCommandScheduler.schedule(new SpawnCharacterCommand(newPlayerCharacter, null), SPAWN_TIME);
    }
}

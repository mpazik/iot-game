package dzida.server.app.instance.command;

import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.CharacterSpawned;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.skill.SkillSate;

import java.util.List;

import static java.util.Collections.singletonList;

public class SpawnCharacterCommand implements InstanceCommand {
    public final Character character;

    public SpawnCharacterCommand(Character character) {
        this.character = character;
    }

    @Override
    public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
        SkillSate.SkillData initialSkillData = state.getSkill().getInitialSkillData(character.getType());
        Move initialMove = state.getPosition().getInitialMove(state.getWorld().getSpawnPoint(), currentTime, definitions.getPlayerSpeed());
        return Outcome.ok(singletonList(new CharacterSpawned(character, initialMove, initialSkillData)));
    }
}

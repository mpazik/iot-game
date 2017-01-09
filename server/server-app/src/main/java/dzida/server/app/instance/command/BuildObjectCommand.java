package dzida.server.app.instance.command;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.world.WorldObjectCreated;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;

import java.util.List;

public class BuildObjectCommand implements InstanceCommand {
    public final Id<WorldObjectKind> objectKindId;
    public final double x;
    public final double y;
    public final Id<Character> characterId;

    public BuildObjectCommand(Id<Character> characterId, Id<WorldObjectKind> objectKindId, double x, double y) {
        this.objectKindId = objectKindId;
        this.x = x;
        this.y = y;
        this.characterId = characterId;
    }

    @Override
    public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
        Id<Character> casterId = characterId;
        if (!state.getCharacter().isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }

        if (state.getSkill().isOnCooldown(casterId, currentTime)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        GeneralEntity<WorldObject> worldObject = state.getWorld().createWorldObject(objectKindId, (int) x, (int) y, currentTime);
        return Outcome.ok(ImmutableList.of(
                new WorldObjectCreated(worldObject)
        ));
    }
}

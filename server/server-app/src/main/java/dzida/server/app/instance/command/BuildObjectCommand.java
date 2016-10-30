package dzida.server.app.instance.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.world.object.WorldObjectKind;

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
}

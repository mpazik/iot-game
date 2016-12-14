package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import org.jetbrains.annotations.NotNull;

public class CharacterGotDamage implements CharacterEvent {
    public final Id<Character> characterId;
    public final double damage;

    public CharacterGotDamage(Id<Character> characterId, double damage) {
        this.characterId = characterId;
        this.damage = damage;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return characterId;
    }
}

package dzida.server.app.instance.command;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.CharacterDied;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.event.ServerMessage;
import dzida.server.app.instance.skill.event.CharacterGotDamage;
import dzida.server.app.instance.skill.event.CharacterHealed;

import java.util.ArrayList;
import java.util.List;

public interface InstanceCommand {

    Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime);

    class EatAppleCommand implements InstanceCommand {

        public final Id<Character> characterId;

        public EatAppleCommand(Id<Character> characterId) {
            this.characterId = characterId;
        }

        @Override
        public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
            Id<Character> casterId = characterId;
            if (!state.getCharacter().isCharacterLive(casterId)) {
                return Outcome.error("Skill can not be used by a not living character.");
            }

            int missingHealth = state.getSkill().getMaxHealth(casterId) - state.getSkill().getHealth(casterId);
            int appleHealingValue = 50;
            int toHeal = Math.min(missingHealth, appleHealingValue);
            return Outcome.ok(ImmutableList.of(new CharacterHealed(casterId, toHeal)));
        }
    }

    class EatRottenAppleCommand implements InstanceCommand {
        public final Id<Character> characterId;

        public EatRottenAppleCommand(Id<Character> characterId) {
            this.characterId = characterId;
        }

        @Override
        public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
            Id<Character> casterId = characterId;
            if (!state.getCharacter().isCharacterLive(casterId)) {
                return Outcome.error("Skill can not be used by a not living character.");
            }

            int damage = 10;
            List<GameEvent> events = new ArrayList<>();
            events.add(new CharacterGotDamage(casterId, damage));
            events.add(new ServerMessage("You ate rotten apple."));
            if (state.getSkill().getHealth(casterId) <= damage) {
                // the got damage event must be before the died event
                events.add(new CharacterDied(casterId));
            }

            return Outcome.ok(events);
        }
    }
}

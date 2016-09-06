package dzida.server.app.instance;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.MoveCommand;
import dzida.server.app.instance.command.SkillUseOnCharacterCommand;
import dzida.server.app.instance.command.SkillUseOnWorldMapCommand;
import dzida.server.app.instance.command.SkillUseOnWorldObjectCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.skill.Skill;
import dzida.server.core.world.object.WorldObject;

public interface CharacterCommand {
    ImmutableSet<Class<?>> classes = ImmutableSet.of(
            Move.class,
            UseSkillOnCharacter.class,
            UseSkillOnWorldMap.class,
            UseSkillOnWorldObject.class,
            EatApple.class,
            EatRottenApple.class
    );

    InstanceCommand getInstanceCommand(Id<Character> characterId);

    class Move implements CharacterCommand {
        public final double x;
        public final double y;
        public final double speed;

        public Move(double x, double y, double speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new MoveCommand(characterId, x, y, speed);
        }
    }

    class UseSkillOnCharacter implements CharacterCommand {
        public final Id<Skill> skillId;
        public final Id<Character> target;

        public UseSkillOnCharacter(Id<Skill> skillId, Id<Character> target) {
            this.skillId = skillId;
            this.target = target;
        }

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new SkillUseOnCharacterCommand(characterId, skillId, target);
        }
    }

    class UseSkillOnWorldMap implements CharacterCommand {
        public final Id<Skill> skillId;
        public final double x;
        public final double y;

        private UseSkillOnWorldMap(Id<Skill> skillId, double x, double y) {
            this.skillId = skillId;
            this.x = x;
            this.y = y;
        }

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new SkillUseOnWorldMapCommand(characterId, skillId, x, y);
        }
    }

    class UseSkillOnWorldObject implements CharacterCommand {
        public final Id<Skill> skillId;
        public final Id<WorldObject> target;

        private UseSkillOnWorldObject(Id<Skill> skillId, Id<WorldObject> target) {
            this.skillId = skillId;
            this.target = target;
        }

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new SkillUseOnWorldObjectCommand(characterId, skillId, target);
        }
    }

    class EatApple implements CharacterCommand {

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new InstanceCommand.EatAppleCommand(characterId);
        }
    }

    class EatRottenApple implements CharacterCommand {

        @Override
        public InstanceCommand getInstanceCommand(Id<Character> characterId) {
            return new InstanceCommand.EatRottenAppleCommand(characterId);
        }
    }
}

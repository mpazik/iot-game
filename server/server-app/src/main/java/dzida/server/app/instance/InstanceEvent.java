package dzida.server.app.instance;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.parcel.ParcelChange;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.CharacterHealed;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;

public interface InstanceEvent {
    ImmutableSet<Class<?>> classes = ImmutableSet.of(
            CharacterSpawned.class,
            CharacterDied.class,
            CharacterMoved.class,
            SkillUsedOnCharacter.class,
            CharacterGotDamage.class,
            StateSynchroniser.InitialData.class,
            ServerMessage.class,
            SkillUsedOnWorldMap.class,
            WorldObjectCreated.class,
            SkillUsedOnWorldObject.class,
            WorldObjectRemoved.class,
            Instance.UserCharacter.class,
            CharacterHealed.class,
            ParcelChange.ParcelClaimed.class
    );
}

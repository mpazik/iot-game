package dzida.server.app.instance;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.instance.character.event.CharacterDied;
import dzida.server.app.instance.character.event.CharacterSpawned;
import dzida.server.app.instance.event.ServerMessage;
import dzida.server.app.instance.position.event.CharacterMoved;
import dzida.server.app.instance.skill.event.CharacterGotDamage;
import dzida.server.app.instance.skill.event.CharacterHealed;
import dzida.server.app.instance.skill.event.SkillUsedOnCharacter;
import dzida.server.app.instance.skill.event.SkillUsedOnWorldMap;
import dzida.server.app.instance.skill.event.SkillUsedOnWorldObject;
import dzida.server.app.instance.world.event.WorldObjectCreated;
import dzida.server.app.instance.world.event.WorldObjectRemoved;
import dzida.server.app.parcel.ParcelChange;

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

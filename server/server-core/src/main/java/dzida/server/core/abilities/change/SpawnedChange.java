package dzida.server.core.abilities.change;

import dzida.server.core.entity.Change;
import dzida.server.core.abilities.Abilities;

public class SpawnedChange implements Change<Abilities> {
    public final int maxHealth;

    public SpawnedChange(int maxHealth) {
        this.maxHealth = maxHealth;
    }
}

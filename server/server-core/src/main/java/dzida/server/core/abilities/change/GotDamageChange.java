package dzida.server.core.abilities.change;

import dzida.server.core.entity.Change;
import dzida.server.core.abilities.Abilities;

public class GotDamageChange implements Change<Abilities> {
    public final int damage;

    public GotDamageChange(int damage) {
        this.damage = damage;
    }
}


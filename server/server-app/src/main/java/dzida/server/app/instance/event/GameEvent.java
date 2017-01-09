package dzida.server.app.instance.event;

import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;

import javax.annotation.Nonnull;

public interface GameEvent {
    @Nonnull
    GameState updateState(@Nonnull GameState state, GameDefinitions definitions);
}
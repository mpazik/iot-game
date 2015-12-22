package dzida.server.core.player;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerData {
    String nick;
    int highestDifficultyLevel;
    int lastDifficultyLevel;
}

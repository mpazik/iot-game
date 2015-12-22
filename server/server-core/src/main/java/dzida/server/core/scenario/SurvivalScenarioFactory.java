package dzida.server.core.scenario;

import lombok.Value;

import java.time.Duration;

public class SurvivalScenarioFactory {

    public SurvivalScenario createSurvivalScenario(int difficultyLevel) {
        Duration time = countTimeFromDifficultyLevel(difficultyLevel);
        int botLevel = countBotLevel(difficultyLevel);
        return new SurvivalScenario(difficultyLevel, time, botLevel, 3, Duration.ofSeconds(5));
    }

    private int countBotLevel(int difficultyLevel) {
        return (int)(Math.pow(1.1, difficultyLevel) * 100);
    }

    private static Duration countTimeFromDifficultyLevel(int difficultyLevel) {
        // time is longer by 15s each 3 levels.
        int timeBusts = difficultyLevel / 3;
        return Duration.ofSeconds(60 + timeBusts * 15);
    }

    @Value
    public final static class SurvivalScenario {
        int difficultyLevel;
        Duration time;
        int botLevel;
        int maxPlayerDeaths;
        Duration botSpawnTime;
    }
}


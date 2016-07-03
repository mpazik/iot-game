package dzida.server.core.scenario;

import java.time.Duration;

public class SurvivalScenarioFactory {

    public SurvivalScenario createSurvivalScenario(int difficultyLevel) {
        int numberOfNpcToKill = countNumberOfNpcToKill(difficultyLevel);
        int botLevel = countBotLevel(difficultyLevel);
        return new SurvivalScenario(difficultyLevel, numberOfNpcToKill, botLevel, Duration.ofSeconds(2));
    }

    private int countBotLevel(int difficultyLevel) {
        return (int)(Math.pow(1.1, difficultyLevel) * 100);
    }

    private static int countNumberOfNpcToKill(int difficultyLevel) {
        return difficultyLevel;
    }

    public final static class SurvivalScenario {
        public final int difficultyLevel;
        public final int numberOfNpcToKill;
        public final int botLevel;
        public final Duration botSpawnTime;

        public SurvivalScenario(int difficultyLevel, int numberOfNpcToKill, int botLevel, Duration botSpawnTime) {
            this.difficultyLevel = difficultyLevel;
            this.numberOfNpcToKill = numberOfNpcToKill;
            this.botLevel = botLevel;
            this.botSpawnTime = botSpawnTime;
        }
    }
}


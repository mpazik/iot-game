package dzida.server.core.position.model;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.basic.unit.Point;
import org.junit.Test;
import org.junit.runner.RunWith;

import static dzida.server.core.assertions.Assertions.assertThat;

@RunWith(HierarchicalContextRunner.class)
public class MoveTest {
    private final long velocity = 1;
    private final Point startPosition = Point.of(1, 1);
    private final Point middlePosition = Point.of(4, 5);
    private final Point endPosition = Point.of(1, 9);
    private final double pathLength = startPosition.distanceTo(middlePosition) + middlePosition.distanceTo(endPosition);

    private final long startTime = 1000;
    private final long endTime = startTime + countDuration(pathLength, velocity);
    private final long middleTime = startTime + (endTime - startTime) / 2;

    private final Move move = Move.of(startTime, velocity, startPosition, middlePosition, endPosition);

    private long countDuration(double length, long velocity) {
        return (long) (length / velocity * 1000);
    }

    public class PositionAtTime {

        public class ForMoveFromSinglePoint {
            private final Point position = Point.of(3, 3);
            Move move = Move.of(startTime, velocity, position);

            @Test
            public void returnsSamePositionForMoveTime() {
                assertThat(move).hasPositionAtTime(startTime, position);
            }

            @Test
            public void returnsSamePositionForTimeBeforeMoveStarted() {
                assertThat(move).hasPositionAtTime(startTime - 1000, position);
            }

            @Test
            public void returnsSamePositionForTimeAfterMoveStarted() {
                assertThat(move).hasPositionAtTime(startTime + 1000, position);
            }
        }

        public class ForMoveFromManyPoints {

            @Test
            public void returnsStartPositionForStartTime() {
                assertThat(move).hasPositionAtTime(startTime, startPosition);
            }

            @Test
            public void returnsEndPositionForEndTime() {
                assertThat(move).hasPositionAtTime(endTime, endPosition);
            }

            @Test
            public void returnsMiddlePositionForMiddleTime() {
                assertThat(move).hasPositionAtTime(middleTime, middlePosition);
            }

            @Test
            public void returnsHalfWayBetweenFirstAndSecondPositionForQuarterTime() {
                long quarterTime = startTime + (endTime - startTime) / 4;
                Point quarterPosition = Point.of(2.5, 3);

                assertThat(move).hasPositionAtTime(quarterTime, quarterPosition);
            }
        }
    }

    public class ContinueMoveTo {
        private final Point newPosition = Point.of(-1, -1);

        @Test
        public void returnsMoveIsWithoutOldPositions_WhenNewMoveIsAddedBeforeMoveStarted() {
            Move newMove = move.continueMoveTo(startTime - 100, velocity, newPosition);

            assertThat(newMove)
                    .hasPositionAtTime(startTime - 1000, newPosition)
                    .hasPositionAtTime(startTime, newPosition)
                    .hasPositionAtTime(endTime, newPosition)
                    .hasPositionAtTime(endTime + 1000, newPosition);
        }

        @Test
        public void returnedMoveThatIsSameTillNewMove() {
            long middleTime = startTime + (endTime - startTime) / 2;
            long thirdQuarterTime = startTime + (endTime - startTime) * 3 / 4;
            Point thirdQuarterPosition = Point.of(2.5, 7);
            long newVelocity = velocity * 2;
            Move newMove = move.continueMoveTo(thirdQuarterTime, newVelocity, newPosition);
            long newEndTime = thirdQuarterTime + countDuration(thirdQuarterPosition.distanceTo(newPosition), newVelocity);

            assertThat(newMove)
                    .hasPositionAtTime(startTime, startPosition)
                    .hasPositionAtTime(middleTime, middlePosition)
                    .hasPositionAtTime(thirdQuarterTime, thirdQuarterPosition)
                    .hasPositionAtTime(newEndTime, newPosition);
        }

        @Test
        public void returnedMoveHasPauseUntilMoveToNewPositionHasNotBegun() {
            long startTimeOfMoveToNewPosition = endTime + 1000;
            Move newMove = move.continueMoveTo(startTimeOfMoveToNewPosition, velocity, newPosition);
            long newEndTime = startTimeOfMoveToNewPosition + countDuration(endPosition.distanceTo(newPosition), velocity);

            assertThat(newMove)
                    .hasPositionAtTime(endTime, endPosition)
                    .hasPositionAtTime(startTimeOfMoveToNewPosition, endPosition)
                    .hasPositionAtTime(newEndTime, newPosition);
        }

        @Test
        public void returnedMoveHasMultipleNewPositions() {
            Point newPosition2 = Point.of(-3, -3);
            Move newMove = move.continueMoveTo(middleTime, velocity, newPosition, newPosition2);

            long timeToNewPos = middleTime + countDuration(middlePosition.distanceTo(newPosition), velocity);
            long newEndTime = timeToNewPos + countDuration(newPosition.distanceTo(newPosition2), velocity);

            assertThat(newMove)
                    .hasPositionAtTime(startTime, startPosition)
                    .hasPositionAtTime(middleTime, middlePosition)
                    .hasPositionAtTime(timeToNewPos, newPosition)
                    .hasPositionAtTime(newEndTime, newPosition2);
        }
    }

    public class CompactHistory {

        @Test
        public void leaveOnlyLastPosition_WhenCompactTimeIsAfterMoveFinished() {
            Move newMove = move.compactHistory(endTime + 1);
            assertThat(newMove)
                    .hasPositionAtTime(startTime, endPosition)
                    .hasPositionAtTime(middleTime, endPosition)
                    .hasPositionAtTime(endTime, endPosition);
        }

        @Test
        public void changeNothing_WhenCompactTimeIsAfterMoveFinished() {
            Move newMove = move.compactHistory(startTime - 100);
            assertThat(newMove)
                    .hasPositionAtTime(startTime, startPosition)
                    .hasPositionAtTime(middleTime, middlePosition)
                    .hasPositionAtTime(endTime, endPosition);
        }

        @Test
        public void removesNotUsedPositions_WhenCompactTimePassThem() {
            Move newMove = move.compactHistory(middleTime);
            assertThat(newMove)
                    .hasPositionAtTime(startTime, middlePosition)
                    .hasPositionAtTime(middleTime, middlePosition)
                    .hasPositionAtTime(endTime, endPosition);
        }
    }

    public class angleAtTime {
        private final double zeroAngle = 0;
        private final double startAngle = -0.6435011087932844;
        private final double middleAngle = 0.6435011087932844;
        private final double endAngle = 0.6435011087932844;

        @Test
        public void returns0_WhenMoveHasSinglePoint() {
            Move move = Move.of(startTime, velocity, startPosition);

            assertThat(move)
                    .hasAngleAtTime(startTime - 100, zeroAngle)
                    .hasAngleAtTime(startTime, zeroAngle)
                    .hasAngleAtTime(middleTime, zeroAngle)
                    .hasAngleAtTime(endTime, zeroAngle)
                    .hasAngleAtTime(endTime + 100, zeroAngle);
        }

        @Test
        public void returns0_WhenTimeIsBeforeMoveStart() {
            assertThat(move).hasAngleAtTime(startTime - 1000, zeroAngle);
        }

        @Test
        public void returnsLastAngle_WhenTimeIsAfterMoveStart() {
            assertThat(move).hasAngleAtTime(endTime + 1000, endAngle);
        }

        @Test
        public void returnsProperAngle_ForGivenMoveTime() {
            long quarterTime = startTime + (endTime - startTime) / 4;
            assertThat(move)
                    .hasAngleAtTime(startTime, startAngle)
                    .hasAngleAtTime(quarterTime, startAngle)
                    .hasAngleAtTime(middleTime, middleAngle)
                    .hasAngleAtTime(endTime, endAngle);
        }
    }
}
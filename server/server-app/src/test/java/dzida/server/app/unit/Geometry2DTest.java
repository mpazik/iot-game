package dzida.server.app.unit;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import dzida.server.app.basic.unit.Point;
import org.junit.Test;
import org.junit.runner.RunWith;

import static dzida.server.app.basic.unit.Geometry2D.getIntersection;
import static dzida.server.app.basic.unit.Geometry2D.isIntersecting;
import static dzida.server.app.basic.unit.Geometry2D.isPointOnLine;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(HierarchicalContextRunner.class)
public class Geometry2DTest {

    public class IsPointOnLine {

        @Test
        public void pointIsInTheMiddleOfLIne_isTrue() {
            assertThat(isPointOnLine(2, 1, 1, 1, 3, 1)).isTrue();
        }

        @Test
        public void pointIsOnLineEdge_isFalse() {
            assertThat(isPointOnLine(1, 1, 1, 1, 3, 1)).isTrue();
            assertThat(isPointOnLine(3, 1, 1, 1, 3, 1)).isTrue();
        }

        @Test
        public void pointIsOutSideOfLine_isFalse() {
            assertThat(isPointOnLine(2, 2, 1, 1, 3, 1)).isFalse();
        }

        @Test
        public void pointWouldBeOnLineIfLineWouldBeLonger_isFalse() {
            assertThat(isPointOnLine(4, 1, 1, 1, 3, 1)).isFalse();
        }
    }

    public class IsIntersecting {

        @Test
        public void linesIntersect_isTrue() {
            assertThat(isIntersecting(1, 1, 3, 1, 2, 0, 2, 2)).isTrue();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 2, 2, 0)).isTrue();
            assertThat(isIntersecting(1, 1, 3, 1, 0, 0, 2, 1.1)).isTrue();
        }

        @Test
        public void linesNotIntersect_isFalse() {
            assertThat(isIntersecting(1, 1, 3, 1, 4, 0, 4, 2)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 4, 2, 4, 0)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 0, 2, 0.9)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 1.1, 2, 1.2)).isFalse();
        }

        @Test
        public void linesOnlyTouchThemSelf_isFalse() {
            assertThat(isIntersecting(1, 1, 3, 1, 1, 1, 3, 1)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 0, 2, 1)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 1, 2, 0)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 2, 2, 2, 1)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 1, 0, 1, 2)).isFalse();
            assertThat(isIntersecting(1, 1, 3, 1, 3, 2, 3, 0)).isFalse();
        }
    }

    public class GetIntersection {
        
        @Test
        public void linesAreIntersecting_intersectionPointIsDefined() {
            assertThat(getIntersection(1, 1, 3, 1, 2, 0, 2, 2)).hasValue(Point.of(2, 1));
            assertThat(getIntersection(1, 1, 3, 1, 2, 2, 2, 0)).hasValue(Point.of(2, 1));
        }

        @Test
        public void lineAreNotIntersecting_noIntersectionPoint() {
            assertThat(getIntersection(1, 1, 3, 1, 4, 0, 4, 2)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 4, 2, 4, 0)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 2, 0, 2, 0.9)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 2, 1.1, 2, 1.2)).isEmpty();
        }

        @Test
        public void linesOnlyTouchThemSelf_noIntersectionPoint() {
            assertThat(getIntersection(1, 1, 3, 1, 1, 1, 3, 1)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 2, 0, 2, 1)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 2, 1, 2, 0)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 2, 2, 2, 1)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 1, 0, 1, 2)).isEmpty();
            assertThat(getIntersection(1, 1, 3, 1, 3, 2, 3, 0)).isEmpty();
        }
    }
}
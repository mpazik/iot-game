package dzida.server.core.basic.unit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineTest {

    private final Line line = Line.of(1, 1, 3, 1);

    @Test
    public void LinesAreIntersecting() {
        assertThat(line.isIntersecting(Line.of(2, 0, 2, 2))).isTrue();
        assertThat(line.isIntersecting(Line.of(2, 2, 2, 0))).isTrue();
        assertThat(line.isIntersecting(Line.of(0, 0, 2, 1.1))).isTrue();
    }

    @Test
    public void LinesAreNotIntersecting() {
        assertThat(line.isIntersecting(Line.of(4, 0, 4, 2))).isFalse();
        assertThat(line.isIntersecting(Line.of(4, 2, 4, 0))).isFalse();
        assertThat(line.isIntersecting(Line.of(2, 0, 2, 0.9))).isFalse();
        assertThat(line.isIntersecting(Line.of(2, 1.1, 2, 1.2))).isFalse();
    }

    @Test
    public void LinesAreNotIntersectingWhenTheyOnlyTouch() {
        assertThat(line.isIntersecting(line)).isFalse();
        assertThat(line.isIntersecting(Line.of(2, 0, 2, 1))).isFalse();
        assertThat(line.isIntersecting(Line.of(2, 1, 2, 0))).isFalse();
        assertThat(line.isIntersecting(Line.of(2, 2, 2, 1))).isFalse();
        assertThat(line.isIntersecting(Line.of(1, 0, 1, 2))).isFalse();
        assertThat(line.isIntersecting(Line.of(3, 2, 3, 0))).isFalse();

    }
}
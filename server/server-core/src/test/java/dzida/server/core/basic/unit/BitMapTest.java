package dzida.server.core.basic.unit;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BitMapTest {

    @Test
    public void inverseBitMapWorks() {
        BitMap bitMap = BitMap.createBitMap(
                " ## ",
                "### ",
                "##  ",
                "  ##"
        );
        BitMap invertedBitMap = ((BitMap.InverseBitMap) BitMap.InverseBitMap.of(bitMap)).toImmutableBitMap();
        assertThat(invertedBitMap).isEqualTo(BitMap.createBitMap(
                "#  #",
                "   #",
                "  ##",
                "##  "
        ));
    }
}
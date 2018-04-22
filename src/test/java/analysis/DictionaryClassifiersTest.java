package analysis;

import org.junit.jupiter.api.Test;

import static analysis.DictionaryClassifiers.visualGenomeVote;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DictionaryClassifiersTest {

    @Test
    void testWordNetVote() {
        assertEquals(0, visualGenomeVote("cheek", "brow", "red",
                "src/test/java/resources/WNNoHyp"), "WordNet without hypernym expansion shouldn't recognize cheek,brow,red as discriminative");
        assertEquals(1, visualGenomeVote("hammer", "wheel", "handle",
                "src/test/java/resources/WNNoHyp"), "Does not recognize hammer,wheel,handle as discriminative");
        assertEquals(1, visualGenomeVote("cheek", "brow", "red",
                "src/test/java/resources/WNWithHyp"), "WordNet with hypernym expansion should recognize cheek,brow,red as discriminative");
        assertEquals(0, visualGenomeVote("neck", "throat", "wide",
                "src/test/java/resources/WNNoHyp"), "WN shouldn't recognize neck,throat,wide as discriminative");
    }

    @Test
    void testVisualGenomeVote() {
        assertEquals(1, visualGenomeVote("banana", "strawberry", "yellow",
                "src/test/java/resources/VG"), "Does not recognize triple banana,strawberry,yellow as discriminative");
        assertEquals(0, visualGenomeVote("chimp", "giraffe", "fur",
                "src/test/java/resources/VG"), "Incorrectly recognizes chimp,giraffe,fur as discriminative");
    }
}
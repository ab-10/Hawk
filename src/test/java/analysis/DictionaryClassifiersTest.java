package analysis;

import org.junit.jupiter.api.Test;

import static analysis.DictionaryClassifiers.visualGenomeVote;
import static analysis.DictionaryClassifiers.wordNetVote;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DictionaryClassifiersTest {

    @Test
    void testWordNetVote() {
        assertEquals(0, wordNetVote("cheek", "brow", "red",
                "src/test/resources/WNNoHyp"), "WordNet without hypernym expansion shouldn't recognize cheek,brow,red as discriminative");
        assertEquals(1, wordNetVote("hammer", "wheel", "handle",
                "src/test/resources/WNNoHyp"), "Does not recognize hammer,wheel,handle as discriminative");
        assertEquals(1, wordNetVote("cheek", "brow", "red",
                "src/test/resources/WNWithHyp"), "WordNet with hypernym expansion should recognize cheek,brow,red as discriminative");
        assertEquals(0, wordNetVote("neck", "throat", "wide",
                "src/test/resources/WNNoHyp"), "WN shouldn't recognize neck,throat,wide as discriminative");
    }

    @Test
    void testVisualGenomeVote() {
        assertEquals(1, visualGenomeVote("banana", "strawberry", "yellow",
                "src/test/resources/VG"), "Does not recognize triple banana,strawberry,yellow as discriminative");
        assertEquals(0, visualGenomeVote("chimp", "giraffe", "fur",
                "src/test/resources/VG"), "Incorrectly recognizes chimp,giraffe,fur as discriminative");
    }
}
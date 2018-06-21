package analysis;

import org.junit.jupiter.api.Test;

import static analysis.DictionaryClassifiers.visualGenomeVote;
import static analysis.DictionaryClassifiers.wordNetVote;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DictionaryClassifiersTest {

    @Test
    void testWordNetVote() {
        assertEquals(1, wordNetVote("cheek", "brow", "red",
                "src/main/resources/WNWithHyp"), "WordNet with hypernym expansion should recognize cheek,brow,red as discriminative");
        assertEquals(1, wordNetVote("neck", "throat", "wide",
                "src/main/resources/WNWithHyp"), "WN should recognize neck,throat,wide as discriminative");
        assertEquals(0, wordNetVote("bus", "train", "transport",
                "src/main/resources/WNWithHyp"), "WN shouldn't recognize bus,train,transport as discriminative");
    }

    @Test
    void testVisualGenomeVote() {
        assertEquals(1, visualGenomeVote("king", "prince", "crown",
                "src/main/resources/oldIndexes/VG"), "Does not recognize triple king,prince,crown as discriminative");
        assertEquals(0, visualGenomeVote("chimp", "giraffe", "fur",
                "src/main/resources/VG"), "Incorrectly recognizes chimp,giraffe,fur as discriminative");
    }
}
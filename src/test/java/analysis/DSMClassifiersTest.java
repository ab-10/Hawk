package analysis;

import org.junit.jupiter.api.Test;

import static analysis.DSMClassifiers.gloVeVote;
import static analysis.DSMClassifiers.lsaVote;
import static analysis.DSMClassifiers.w2vVote;
import static org.junit.jupiter.api.Assertions.*;

class DSMClassifiersTest {

    @Test
    void w2vVoteTest() throws Exception{
        assertEquals(1, w2vVote("carriages", "cab", "horse"));
        assertEquals(0, w2vVote("gun", "bomb", "black"));
    }

    @Test
    void lsaVoteTest() throws Exception{
        assertEquals(0, lsaVote("superstructure", "deck", "build"));
        assertEquals(1, lsaVote("olive", "lemon", "black"));
    }

    @Test
    void gloVeVoteTest() throws Exception{
        assertEquals(1, gloVeVote("pie", "pumpkin", "filling"));
        assertEquals(0, gloVeVote("day", "night", "bright"));
    }
}
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definition;
import prep.WNGraph;

public class WNGraphTests {
    private static WNGraph WNGraph;

    @BeforeAll
    public static void initialize() {
        WNGraph = new WNGraph("reduced_WN_model.rdf");
    }

    @Test
    public void findsIndividualDefinitions() {
        assertEquals("trigger", WNGraph.getDefinition("trigger").getDefinienda().get(1)
                , "WNGraph does not create a definition with correct definiendum");

        assertEquals("genus Centrocercus", WNGraph.getDefinition("genus Centrocercus").getDefinienda().get(1)
                , "WNGraph does not create a definition with correct definiendum");

    }

    @Test void listsPropertiesOfDefinitions(){
        Definition trigger = WNGraph.getDefinition("trigger");
        Definition centrocerus = WNGraph.getDefinition("genus Centrocercus");
        assertEquals(2, trigger.getProperties().size(),
                "WNGraph does not populate the definition with the correct number of properties");
        assertEquals(2, centrocerus.getProperties().size(),
                "WNGraph does not populate the definition with the correct number of properties");

    }

    @Test void getsAllDefinitions(){
        assertEquals(3, WNGraph.getAllDefinitions().size(),
                "WNGraph doesn't retrieve all of the definitions");
    }
}

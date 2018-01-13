import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definition;
import prep.Graph;

public class GraphTests {
    private static Graph graph;

    @BeforeAll
    public static void initialize() {
        graph = new Graph("reduced_WN_model.rdf");
    }

    @Test
    public void findsIndividualDefinitions() {
        assertEquals("trigger", graph.getDefinition("trigger").getDefinienda().get(1)
                , "Graph does not create a definition with correct definiendum");

        assertEquals("genus Centrocercus", graph.getDefinition("genus Centrocercus").getDefinienda().get(1)
                , "Graph does not create a definition with correct definiendum");

    }

    @Test void listsPropertiesOfDefinitions(){
        Definition trigger = graph.getDefinition("trigger");
        Definition centrocerus = graph.getDefinition("genus Centrocercus");

        assertEquals(1, trigger.getProperties().size(),
                "Graph does not populate the definition with the correct number of properties");
        assertEquals(1, centrocerus.getProperties().size(),
                "Graph does not populate the definition with the correct number of properties");

    }

    @Test void getsAllDefinitions(){
        assertEquals(3, graph.getAllDefinitions().size(),
                "Graph doesn't retrieve all of the definitions");
    }
}

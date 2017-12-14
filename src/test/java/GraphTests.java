import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definition;
import prep.Graph;

public class GraphTests {
    private Graph graph;

    @BeforeAll
    public void initialize() {
        graph = new Graph("reduced_WN_model.rdf");
    }

    @Test
    public void findsIndividualDefinitions() {
        assertEquals(graph.getDefinition("gun_trigger__trigger").getDefiniendum()
                , "gun_trigger__trigger", "Graph does not create a definition with correct definiendum");

        assertEquals(graph.getDefinition("Centrocercus__genus_Centrocercus").getDefiniendum()
                , "Centrocercus__genus_Centrocercus", "Graph does not create a definition with correct definiendum");

    }

    @Test void listsPropertiesOfDefinitions(){
        Definition trigger = graph.getDefinition("gun_trigger__trigger");
        Definition centrocerus = graph.getDefinition("Centrocercus__genus_Centrocercus");

        assertEquals(1, trigger.getProperties().size(),
                "Graph does not populate the definition with the correct number of properties");
        assertEquals(3, centrocerus.getProperties().size(),
                "Graph does not populate the definition with the correct number of properties");

    }

    @Test void getsAllDefinitions(){
        assertEquals(2, graph.getAllDefinitions().size(),
                "Graph doesn't retrieve all of the definitions");
    }
}

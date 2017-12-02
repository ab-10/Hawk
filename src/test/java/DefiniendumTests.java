import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definiendum;
import prep.Graph;
import prep.Property;

import java.util.ArrayList;

public class DefiniendumTests {
    private static Definiendum apple;
    private static Definiendum PAGAD;
    @BeforeAll public static void initialise(){
        Graph graph = new Graph();
        graph.addModel("WN_DSR_model_XML.rdf");

        apple = graph.findDefiniendum("apple");
        PAGAD = graph.findDefiniendum("People_against_Gangsterism_and_Drugs__PAGAD");
    }

    @Test
    public void canBeRepresentedAsString(){
        assertEquals(apple.toString(), "apple");
        assertEquals(PAGAD.toString(), "People_against_Gangsterism_and_Drugs__PAGAD");
    }

    @Test
    public void findsProperties(){
        ArrayList<Property> appleProperties = apple.findProperties();
        assertEquals(appleProperties.size(), 1);

        ArrayList<Property> PAGADProperties = PAGAD.findProperties();
        assertEquals(PAGADProperties.size(), 3);
    }
}

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definendum;
import prep.Graph;
import prep.Property;

public class DefinendumTests {
    @BeforeAll public void initialise(){
        Graph graph = new Graph();
        graph.addModel("../resources/WN_DSR_model_XML.rdf");

        Definendum apple = graph.findDefinendum("apple");
        Definendum PAGAD = graph.findDefinendum("People_against_Gangsterism_and_Drugs__PAGAD");
    }

    @Test
    public static void canBeRepresentedAsString(){
        assertEquals(apple.toString(), "apple");
        assertEquals(PAGAD.toString(), "People_against_Gangsterism_and_Drugs__PAGAD");
    }

    @Test
    public static void findsProperties(){
        Property[] appleProperties = apple.findProperties();
        assertEquals(appleProperties.length, 1);

        Property[] PAGADProperties = PAGAD.findProperties();
        assertEquals(PAGADProperties.length, 3);
    }
}

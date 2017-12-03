import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.vocabulary.*;

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
        assertEquals("apple", apple.toString()
                , "Definiendum apple is not correctly represented as a String");
        assertEquals("People_against_Gangsterism_and_Drugs__PAGAD", PAGAD.toString()
                , "Definiendum PAGAD is not correctly represented as a String");
    }

    @Test
    public void findsProperties(){
        ArrayList<Property> appleProperties = apple.findProperties();
        assertEquals(1, appleProperties.size()
                , "findProperties() does not find the correct number of properties for the definition of apple");

        ArrayList<Property> PAGADProperties = PAGAD.findProperties();
        assertEquals(3, PAGADProperties.size()
                , "findProperties() does not find the correct number of properties for the definition of PAGAD");
    }
}

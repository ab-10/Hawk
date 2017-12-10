import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import prep.Definition;
import prep.Graph;
import prep.Property;

import java.util.ArrayList;
import java.util.List;

public class DefinitionTests {
    private static Definition apple;
    private static Definition PAGAD;
    @BeforeAll public static void initialise(){
        Graph graph = new Graph();
        graph.addModel("WN_DSR_model_XML.rdf");

        apple = graph.findDefinition("apple");
        PAGAD = graph.findDefinition("People_against_Gangsterism_and_Drugs__PAGAD");
    }

    @Test
    public void canBeRepresentedAsString(){
        assertEquals("apple", apple.toString()
                , "Definition apple is not correctly represented as a String");
        assertEquals("People_against_Gangsterism_and_Drugs__PAGAD", PAGAD.toString()
                , "Definition PAGAD is not correctly represented as a String");
    }

    @Test
    public void listsProperties(){
        List<Property> appleProperties = apple.listProperties();
        assertEquals(1, appleProperties.size()
                , "listProperties() does not find the correct number of properties for the definition of apple");

        List<Property> PAGADProperties = PAGAD.listProperties();
        assertEquals(3, PAGADProperties.size()
                , "listProperties() does not find the correct number of properties for the definition of PAGAD");
    }
}

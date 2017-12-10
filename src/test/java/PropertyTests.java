import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import prep.Definition;
import prep.Graph;
import prep.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyTests {
    private static List<Property> appleProperties;
    private static List<Property> PAGADProperties;


    @BeforeAll
    public static void initialise(){
        Graph graph = new Graph();
        graph.addModel("WN_DSR_model_XML.rdf");

        Definition apple = graph.findDefinition("apple");
        Definition PAGAD = graph.findDefinition("People_against_Gangsterism_and_Drugs__PAGAD");

        appleProperties = apple.listProperties();
        PAGADProperties = PAGAD.listProperties();
    }

    @Test
    public void canFindRoles(){
        assertEquals("has_diff_qual", appleProperties.get(0).getRole()
                , "Fails to identify the correct role for apple's properties");
        assertEquals("has_purpose", PAGADProperties.get(0).getRole()
                , "Fails to identify the correct role for PAGAD's properties");
    }

    @Test
    public void canBeRepresentedAsString(){
        assertEquals("with red or yellow or green skin and sweet to tart crisp whitish flesh", appleProperties.get(0).toString()
                , "Property of Apple number 0 is not correctly represented as a String");
        assertEquals("in South Africa", PAGADProperties.get(0).toString()
                , "Property of PAGAD number 0 is not correctly represented as a String");
    }
}

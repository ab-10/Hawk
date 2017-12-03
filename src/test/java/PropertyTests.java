import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import prep.Definiendum;
import prep.Graph;
import prep.Property;

import java.util.ArrayList;

public class PropertyTests {
    private static ArrayList<Property> appleProperties;
    private static ArrayList<Property> PAGADProperties;


    @BeforeAll
    public static void initialise(){
        Graph graph = new Graph();
        graph.addModel("WN_DSR_model_XML.rdf");

        Definiendum apple = graph.findDefiniendum("apple");
        Definiendum PAGAD = graph.findDefiniendum("People_against_Gangsterism_and_Drugs__PAGAD");

        appleProperties = apple.findProperties();
        PAGADProperties = PAGAD.findProperties();
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

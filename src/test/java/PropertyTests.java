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
        graph.addModel("../resources/WN_DSR_model_XML.rdf");

        Definiendum apple = graph.findDefiniendum("apple");
        Definiendum PAGAD = graph.findDefiniendum("People_against_Gangsterism_and_Drugs__PAGAD");

        appleProperties = apple.findProperties();
        PAGADProperties = PAGAD.findProperties();
    }

    @Test
    public static void canFindRoles(){
        assertEquals(appleProperties[0].getRole(), "has_diff_qual");
        assertEquals(PAGADProperties[0].getRole(), "has_purpose");
    }

    @Test
    public static void canBeRepresentedAsString(){
        assertEquals(appleProperties[0].toString(), "with red or yellow or green skin and sweet to tart crisp whitish flesh");
        assertEquals(PAGADProperties[0].toString(), "to fight drug lords");
    }
}

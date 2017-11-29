import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import prep.Definendum;
import prep.Graph;
import prep.Property;

public class PropertyTests {
    @BeforeAll
    public static void initialise(){
        Graph graph = new Graph();
        graph.addModel("../resources/WN_DSR_model_XML.rdf");

        Definendum apple = graph.findDefinendum("apple");
        Definendum PAGAD = graph.findDefinendum("People_against_Gangsterism_and_Drugs__PAGAD");

        Property[] appleProperties = apple.findProperties();
        Property[] PAGADProperties = PAGAD.findProperties();
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

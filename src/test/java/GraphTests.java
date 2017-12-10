import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import prep.Definition;
import prep.Graph;

public class GraphTests {
    @Test
    public void readsAnRDFModel() {
       Graph graph = new Graph();

       try{
           graph.addModel("non_existent_model.rdf");
           fail("Exception should be thrown");
       } catch (IllegalArgumentException exception){
           assertEquals(exception.getMessage(), "File: non_existent_model.rdf not found");
       }

       try {
           graph.addModel("WN_DSR_model_XML.rdf");
       } catch (Exception e){
           fail("No exception should be thrown");
       }
    }

    @Test
    public void findsDefinition() {
        Graph graph = new Graph();
        graph.addModel("WN_DSR_model_XML.rdf");

        Definition apple = graph.findDefinition("apple");
        assertEquals("apple", apple.toString()
                , "Definition of apple hasn't been found or is not correctly represented as a String");

        Definition PAGAD = graph.findDefinition("People_against_Gangsterism_and_Drugs__PAGAD");
        assertEquals("People_against_Gangsterism_and_Drugs__PAGAD", PAGAD.toString()
                , "Definition of PAGAD hasn't been found or is not correctly represented as a String");
    }
}

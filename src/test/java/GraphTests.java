import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import prep.Definiendum;
import prep.Graph;

public class GraphTests {
    @Test
    public void readsAnRDFModel() {
       Graph graph = new Graph();

       try{
           graph.addModel("../resources/non_existent_model.rdf");
           fail("Exception should be thrown");
       } catch (IllegalArgumentException exception){
           assertEquals(exception.getMessage(), "File: ../resources/non_existent_model.rdf not found");
       }

       try {
           graph.addModel("../resources/WN_DSR_model_XML.rdf");
       } catch (Exception e){
           fail("No exception should be thrown");
       }
    }

    @Test
    public void findsDefinendum() {
        Graph graph = new Graph();
        graph.addModel("../resources/WN_DSR_model_XML.rdf");

        Definiendum apple = graph.findDefiniendum("apple");
        assertEquals(apple.toString(), "Apple");

        Definiendum PAGAD = graph.findDefiniendum("People_against_Gangsterism_and_Drugs__PAGAD");
        assertEquals(PAGAD.toString(), "People_against_Gangsterism_and_Drugs__PAGAD");
    }
}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import prep.Definendum;
import prep.Graph;

public class GraphTests {
    @Test
    public void readsAnRDFModel() {
       Graph graph = new Graph();

       try{
           graph.addModel("../resources/non_existent_model.rdf");
           fail("Exception should be thrown");
       } catch (Exception e){
           ;
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

        Definendum definendum = graph.findDefinendum("Apple");
        assertEquals(definendum.toString, "Apple");
    }
}

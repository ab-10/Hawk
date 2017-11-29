package prep;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.InputStream;

public class Graph {
    private final Model model;
    private final String resourceURIStart = "http://nlp/resources/synsets/WordNetNounSynset#";

    public Graph(){
        this.model = ModelFactory.createDefaultModel();
    }

    public void addModel(String graphLocation){
        InputStream graphFile = FileManager.get().open(graphLocation);
        if (graphFile == null){
            throw new IllegalArgumentException("File: " + graphLocation + " not found");
        }
        this.model.read(graphLocation);
    }

    public Definendum findDefindendum(String definendumName){
        return new Definendum(this.model.getResource(this.resourceURIStart + definendumName))
    }
}

package prep;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;

public class Definiendum {
    private final Resource resource;

    public Definiendum(Resource resource){
        this.resource = resource;
    }

    public ArrayList<Property> findProperties(){
        ArrayList<Property> properties = new ArrayList<Property>();
        StmtIterator propertyIterator = this.resource.listProperties();
        while(propertyIterator.hasNext()){
            try {
                Statement currentStatement = propertyIterator.nextStatement().getProperty(RDF.object);
                properties.add(new Property(currentStatement));
            }catch (PropertyNotFoundException e) {
                ;
            }
        }

        return properties;
    }

    public String toString(){
        return this.resource.getLocalName();
    }

}

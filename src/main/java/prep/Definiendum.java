package prep;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;

public class Definiendum {
    private final Resource resource;

    public Definiendum(Resource resource){
        this.resource = resource;
    }

    public ArrayList<Property> findProperties(){
        ArrayList<Property> properties = new ArrayList<>();
        StmtIterator propertyIterator = this.resource.listProperties(RDF.type);

        while(propertyIterator.hasNext()){
            String value;
            String role;
            Statement currentStatement = propertyIterator.next();

            // if currentStatement is nested it selects the innermost role and value
            // TODO: the model should be improved to also parse the outer roles and values
            // TODO: role should be set to the local role value (as opposed to global)
            if(currentStatement.getProperty(RDF.object).getObject().isLiteral()){
                value = currentStatement.getProperty(RDF.object).getObject().toString();
                role = currentStatement.getProperty(RDF.predicate).getObject().toString();
            }else{
                value = currentStatement.getProperty(RDF.object).getProperty(RDF.object).getObject().toString();
                role = currentStatement.getProperty(RDF.object).getProperty(RDF.predicate).getObject().toString();
            }

            properties.add(new Property(value, role));
        }

        return properties;
    }

    public String toString(){
        return this.resource.getLocalName();
    }

}

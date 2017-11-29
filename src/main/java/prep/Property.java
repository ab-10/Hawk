package prep;


import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.vocabulary.RDF;

public class Property {
    private final Statement statement;

    public Property(Statement statement){
        this.statement = statement;
    }

    public String toString(){
        if(this.statement.getObject() instanceof LiteralImpl){
            return this.statement.getProperty(RDF.object).getObject().toString();
        }else{
            return this.statement.getProperty(RDF.object).getProperty(RDF.object).getObject().toString();
        }
    }

    public String getRole(){
        return this.statement.getProperty(RDF.predicate).getResource().getLocalName();
    }
}

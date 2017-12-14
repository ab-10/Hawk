package prep;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definition {
    private final String URI;
    private final String value;
    private List<Property> properties;

    public Definition(String URI){
        this.URI = URI;
        this.value = generateValue(this.URI);
        this.properties = new ArrayList<>();
    }

    public String getURI() {
        return URI;
    }

    public void addProperty(Property propertyToAdd){
        properties.add(propertyToAdd);
    }

    public void addProperty(List<Property> propertiesToAdd){
        properties.addAll(propertiesToAdd);
    }

    public String toString(){
        String result = "Definition(" + value + ", ";
        for(int i = 0; i < (properties.size() - 1); i++){
            result += properties.get(i) + ", ";
        }
        result += properties.get(properties.size() - 1) + ")";
        return result;
    }

    // Extracts the local name from the URI, for use as the Definition's value
    private static String generateValue(String URI){
        Pattern regExPattern = Pattern.compile("(?<=#).*(?=\\>)");
        Matcher matcherForURI = regExPattern.matcher(URI);
        matcherForURI.find();
        return matcherForURI.group();
    }
}

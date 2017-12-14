package prep;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definition {
    private final String URI;
    private final String definiendum;
    private List<Property> properties;

    public Definition(String URI){
        this.URI = URI;
        this.definiendum = generateValue(this.URI);
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

    public List<Property> getProperties() {
        return properties;
    }

    public String getDefiniendum() {

        return definiendum;
    }

    public String toString(){
        String result = "Definition(" + definiendum + ", ";
        for(int i = 0; i < (properties.size() - 1); i++){
            result += properties.get(i) + ", ";
        }
        result += properties.get(properties.size() - 1) + ")";
        return result;
    }

    // Extracts the local name from the URI, for use as the Definition's definiendum
    private static String generateValue(String URI){
        Pattern regExPattern = Pattern.compile("(?<=#).*(?=\\>)");
        Matcher matcherForURI = regExPattern.matcher(URI);
        matcherForURI.find();
        return matcherForURI.group();
    }
}

package prep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definition {
    private final String URI;
    private final List<String> definienda;
    private List<Property> properties;

    public Definition(String URI){
        this.URI = URI;
        this.definienda = generateValue(this.URI);
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

    // todo: write tests for getPropertiesWithRole(String role)
    /**
     * Lists properties with the desired <code>role</code>.
     * @param role desired role.
     * @return returns a list of properties matching the <code>role</code>.
     */
    public List<Property> getPropertiesWithRole(String role){
        List<Property> result = new ArrayList<>();
        for(Property currentProperty : properties){
            if(currentProperty.getRole().equals(role)){
                result.add(currentProperty);
            }
        }
        return result;
    }

    public List<String> getDefinienda() {

        return definienda;
    }

    public String toString(){
        String result = "Definition(";
        for(String definiendum :definienda){
            result += definiendum + ", ";
        }
        for(int i = 0; i < (properties.size() - 1); i++){
            result += properties.get(i) + ", ";
        }
        result += properties.get(properties.size() - 1) + ")";
        return result;
    }

    // Extracts the local name from the URI, for use as the Definition's definienda
    private static List<String> generateValue(String URI){
        Pattern regExPattern = Pattern.compile("(?<=#).*");
        Matcher matcherForURI = regExPattern.matcher(URI);
        matcherForURI.find();
        String relativeURI = matcherForURI.group();
        relativeURI = relativeURI.replaceAll("(?<!_)_(?!_)", " "); // replaces all single "_" occurrences with " "
        return Arrays.asList(relativeURI.split("__"));
    }
}

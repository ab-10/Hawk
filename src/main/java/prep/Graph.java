package prep;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Definition> definitions;

    public Graph(String graphLocation){
        Model model = createPopulatedModel(graphLocation);
        definitions = findAllDefinitions(model);
        populateAllDefinitions(definitions, model);
    }

    private static Model createPopulatedModel(String modelLocation){
        Model modelCreated = ModelFactory.createDefaultModel();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream graphFile = loader.getResourceAsStream(modelLocation);
        // InputStream graphFile = FileManager.get().open(graphLocation);
        if (graphFile == null){
            throw new IllegalArgumentException("File: " + modelLocation + " not found");
        }
        // base URI is null because graphs are assumed to not use relative URIs
        modelCreated.read(graphFile, null);
        return modelCreated;
    }

    private static List<Definition> findAllDefinitions(Model model){
        List<Definition> definitions = new ArrayList<>();
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT DISTINCT ?definition " +
                "WHERE{" +
                "?definition rdf:type ?property ." +
                "FILTER(?property != rdf:Statement)" +
                "}";

        Query SPARQLquery = QueryFactory.create(queryString);
        try(QueryExecution qexec = QueryExecutionFactory.create(SPARQLquery, model)){
            ResultSet results = qexec.execSelect();
            while(results.hasNext()){
                QuerySolution solution = results.nextSolution();
                Resource currentDefinition = solution.getResource("definition");
                definitions.add(new Definition(currentDefinition.getURI()));
            }
        }
        return definitions;
    }

    private static void populateAllDefinitions(List<Definition> definitions, Model model){
        String queryTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?value ?role ?subject " +
                "WHERE{" +
                "<%s>      rdf:type      ?property ." +
                "?property rdf:object    ?value ." +
                "?property rdf:predicate ?role ." +
                "?property rdf:subject   ?subject" +
                "}";
        for(Definition currentDefinition : definitions){
            String currentQueryString = String.format(queryTemplate, currentDefinition.getURI());
            Query currentQuery = QueryFactory.create(currentQueryString);
            try(QueryExecution qexec = QueryExecutionFactory.create(currentQuery, model)){
                ResultSet properties = qexec.execSelect();
                Boolean hasReifiedProperties = false;
                while(properties.hasNext()){
                    QuerySolution currentProperty = properties.nextSolution();
                    String currentValue;
                    // in case an attempt of obtaining a literal of value fails it means that the value is reified
                    try {
                        currentValue = currentProperty.getLiteral("value").toString();
                    } catch (ClassCastException exception){
                        hasReifiedProperties = true;
                        break;

                    }

                    String currentRole = currentProperty.getResource("role").getLocalName();
                    String currentSubject = currentProperty.getResource("subject").getLocalName();
                    // it is safe to use currentValue despite it being initialized only within the try block
                    // because the catch clause breaks the iteration of the loop
                    currentDefinition.addProperty(new Property(currentValue, currentRole, currentSubject));
                }
                // finds all the reified values for the definition and creates properties representing them
                if(hasReifiedProperties) {
                    String reifiedQueryTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "SELECT ?role ?subject ?subValue ?subRole ?subSubject " +
                            "WHERE{ " +
                            "<%s>             rdf:type      ?property ." +
                            "?property        rdf:object    ?subProperty ." +
                            "?property        rdf:predicate ?role ." +
                            "?property        rdf:subject   ?subject ." +
                            "?subProperty     rdf:object    ?subValue ." +
                            "?subProperty     rdf:predicate ?subRole ." +
                            "?subProperty     rdf:subject   ?subSubject" +
                            "}";
                    String reifiedQueryString = String.format(reifiedQueryTemplate, currentDefinition.getURI());
                    Query reifiedQuery = QueryFactory.create(reifiedQueryString);
                    try (QueryExecution reifiedQueryExec = QueryExecutionFactory.create(reifiedQuery, model)) {
                        ResultSet reifiedProperties = reifiedQueryExec.execSelect();
                        while (reifiedProperties.hasNext()) {
                            QuerySolution reifiedProperty = reifiedProperties.nextSolution();
                            String currentValue = reifiedProperty.getLiteral("subValue").toString() + " "
                                    + reifiedProperty.getResource("subSubject").getLocalName();
                            String currentRole = reifiedProperty.getResource("role").getLocalName();
                            String currentSubject = reifiedProperty.getResource("subject").getLocalName();
                            currentDefinition.addProperty(new Property(currentValue, currentRole, currentSubject));
                        }
                    }
                }
            }
        }
    }

    public Definition getDefinition(String definiendum){
        for(Definition currentDefinition: definitions){
            if(currentDefinition.getDefiniendum().equals(definiendum)){
                return currentDefinition;
            }
        }
        return null;
    }

    public List<Definition> getAllDefinitions(){
        return definitions;
    }
}

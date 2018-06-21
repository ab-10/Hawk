package prep;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WNGraph {
    private List<Definition> definitions;
    private Model model;
    private String graphLocation;
    private boolean populated;

    public WNGraph(String graphLocation) {
        this.graphLocation = graphLocation;
        populated = false;
    }

    public void populate(){
        this.createModel();
        this.findAndPopulateDefinitions();
        populated = true;
    }

    private void findAndPopulateDefinitions(){
        this.findAllDefinitions();
        this.populateAllDefinitions();
    }

    private void createModel() {
        model = ModelFactory.createDefaultModel();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream graphFile = loader.getResourceAsStream(graphLocation);
        // InputStream graphFile = FileManager.get().open(graphLocation);
        if (graphFile == null) {
            throw new IllegalArgumentException("File: " + graphLocation + " not found");
        }
        // base URI is null because graphs are assumed to not use relative URIs
        model.read(graphFile, null);
    }

    private void findAllDefinitions() {
        definitions = new ArrayList<>();
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT DISTINCT ?definition " +
                "WHERE{" +
                "?definition rdf:type ?property ." +
                "FILTER(?property != rdf:Statement)" +
                "}";

        Query SPARQLquery = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(SPARQLquery, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Resource currentDefinition = solution.getResource("definition");
                definitions.add(new Definition(currentDefinition.getURI()));
            }
        }
    }

    private void populateAllDefinitions() {
        String propertyTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?value ?role ?subject " +
                "WHERE{" +
                "<%s>      rdf:type      ?property ." +
                "?property rdf:object    ?value ." +
                "?property rdf:predicate ?role ." +
                "?property rdf:subject   ?subject" +
                "}";

        String reifiedPropertyTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
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

        String hypernymTemplate = "PREFIX dsr: <http://nlp/resources/DefinitionSemanticRoles#> " +
                "SELECT ?hypernym " +
                "WHERE{ " +
                "<%s>             dsr:has_supertype     ?hypernym" +
                "}";

        for (Definition currentDefinition : this.definitions) {
            String currentQueryString = String.format(propertyTemplate, currentDefinition.getURI());
            Query currentQuery = QueryFactory.create(currentQueryString);
            try (QueryExecution qexec = QueryExecutionFactory.create(currentQuery, this.model)) {
                ResultSet properties = qexec.execSelect();
                Boolean hasReifiedProperties = false;
                while (properties.hasNext()) {
                    QuerySolution currentProperty = properties.nextSolution();
                    String currentValue;
                    // in case an attempt of obtaining a literal of value fails it means that the value is reified
                    try {
                        currentValue = currentProperty.getLiteral("value").toString();
                    } catch (ClassCastException exception) {
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
                if (hasReifiedProperties) {
                    String reifiedQueryString = String.format(reifiedPropertyTemplate, currentDefinition.getURI());
                    Query reifiedQuery = QueryFactory.create(reifiedQueryString);
                    try (QueryExecution reifiedQueryExec = QueryExecutionFactory.create(reifiedQuery, this.model)) {
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

            String currentHypernymString = String.format(hypernymTemplate, currentDefinition.getURI());
            Query currentHypernymQuery = QueryFactory.create(currentHypernymString);
            try(QueryExecution qexec = QueryExecutionFactory.create(currentHypernymQuery, this.model)){
                ResultSet hypernyms = qexec.execSelect();
                while (hypernyms.hasNext()){
                    QuerySolution currentHypernym = hypernyms.nextSolution();
                    String currentValue = currentHypernym.getResource("hypernym").getLocalName();
                    currentDefinition.addProperty(new Property(currentValue, "has_supertype"));
                }
            }
        }

        // after adding the immediate properties to all definitions, add hypernym properties
        // initialy adds properties to the buffer, so that properties added from a hypernym
        // aren't passed on to a hyponym in a subsequent iteration
        for (Definition definition : this.definitions) {
            // the try catch clause skips all definitions with no hypernyms
            // if definition doesn't include a hypernym getPropertiesWithRole will return an empty
            // list and calling get(0) will invoke a IndexOutOfBoundsException
            try {
                String hypernymDefiniendum = definition.getPropertiesWithRole("has_supertype").get(0).getValue();
                Definition hypernymDefinition = this.getDefinition(hypernymDefiniendum);
                if (hypernymDefinition != null) {
                    definition.addBufferProperty(hypernymDefinition.getProperties());
                }
            } catch (IndexOutOfBoundsException exception) {
            }
        }

        // only after all of the hypernym properties have been added to the buffer add them to main properties
        for (Definition definition : this.definitions) {
            definition.addPropertiesFromBuffer();
        }
    }

    public Definition getDefinition(String definiendum) {
        for (Definition currentDefinition : definitions) {
            if (currentDefinition.getDefinienda().contains(definiendum)) {
                return currentDefinition;
            }
        }
        return null;
    }

    public List<Definition> getAllDefinitions() {
        return definitions;
    }
}

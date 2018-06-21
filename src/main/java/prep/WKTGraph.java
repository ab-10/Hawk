package prep;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;

public class WKTGraph extends Graph {
    public WKTGraph(String graphLocation) {
        super(graphLocation);
    }

    @Override
    protected void findAllDefinitions() {
        definitions = new ArrayList<>();
        String queryString = "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT DISTINCT ?URI, ?definiendum " +
                "WHERE{" +
                "?URI rdf:label ?definiendum ." +
                "}";

        Query SPARQLquery = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(SPARQLquery, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String definiendum = solution.getLiteral("definiendum").toString();
                String uri = solution.getResource("URI").getURI();

                definitions.add(new Definition(definiendum, uri));
            }
        }
    }
}

package prep;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WNGraph extends Graph {

    public WNGraph(String graphLocation) {
        super(graphLocation);
    }

    @Override
    protected void findAllDefinitions() {
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

}

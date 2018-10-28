package demo;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import indexation.GraphIndexer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import prep.Graph;
import prep.Property;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DiscriminativityHandler extends AbstractHandler {
    private final String indexFolderLocation;

    public DiscriminativityHandler(String indexFolderLocation) {
        // Since directory names are appended to this path, it has to end with a slash
        if (indexFolderLocation.charAt(indexFolderLocation.length() - 1) != '/') {
            indexFolderLocation += '/';
        }
        this.indexFolderLocation = indexFolderLocation;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        // A triple is considered discriminative iff
        // pivot has a property with the value of feature
        // such that comparison doesn't have a property with the same value and role

        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        JsonGenerator outputGenerator = new JsonFactory().createGenerator(out);

        String format = baseRequest.getAttribute("format") + "";
        Boolean useHTML = format.equalsIgnoreCase("HTML");

        String pivot, comparison, feature;
        pivot = baseRequest.getParameter("pivot");
        comparison = baseRequest.getParameter("comparison");
        feature = baseRequest.getParameter("feature");

        if(pivot == null | comparison == null | feature == null){
            if (!useHTML) {
                out.println("{Invalid request}");
            }
            return;
        }

        if (useHTML) {
            response.setContentType("text/html");
        } else {
            response.setContentType("application/json");
        }

        if (useHTML) {
            outputGenerator.writeStartObject();
        }

        String[] indexNames = {"WKP_Graph", "WKT", "WN"};
        // Tracks if the triple is discriminative according to at least one model
        Boolean jointlyDiscriminative = false;
        for (String indexName : indexNames) {
            if (useHTML) {
                out.println("<h3>" + indexName + "</h3>");
            } else {
                outputGenerator.writeArrayFieldStart(indexName);
            }

            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolderLocation + indexName)));
            IndexSearcher searcher = new IndexSearcher(reader);

            ScoreDoc[] pivotDocs = searcher.search(new TermQuery(new Term("definiendum", pivot)), 10).scoreDocs;
            ScoreDoc[] comparisonDocs = searcher.search(new TermQuery(new Term("definiendum", comparison)), 10).scoreDocs;

            ArrayList<Property> pivotProperties = new ArrayList<>();
            ArrayList<Property> comparisonProperties = new ArrayList<>();

            for (ScoreDoc result : pivotDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    // Ignores the catch-all field
                    if(! field.name().equals(GraphIndexer.BLIND_FIELD_NAME)) {
                        pivotProperties.add(new Property(field.stringValue(), field.name()));
                    }
                }
            }

            for (ScoreDoc result : comparisonDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    // Ignores the catch-all field
                    if(! field.name().equals(GraphIndexer.BLIND_FIELD_NAME)) {
                        comparisonProperties.add(new Property(field.stringValue(), field.name()));
                    }
                }
            }

            // Selects those properties from comparison and pivot
            // where feature is the value
            ArrayList<Property> pivotFeatureProperties = new ArrayList<>();
            ArrayList<Property> comparisonFeatureProperties = new ArrayList<>();

            pivotProperties.stream()
                    .filter(property -> property.getValue().equals(feature))
                    .forEach(property -> pivotFeatureProperties.add(property));

            comparisonProperties.stream()
                    .filter(property -> property.getValue().equals(feature))
                    .forEach(property -> comparisonFeatureProperties.add(property));


            // Selects the properties with the value of feature and equal roles
            // between pivot's and comparison's properties
            ArrayList<Property> intersectingFeatureProperties = new ArrayList<>();

            pivotFeatureProperties.stream()
                    .filter(property -> comparisonFeatureProperties.contains(property))
                    .forEach(property -> intersectingFeatureProperties.add(property));


            // Makes the discriminativity decision
            boolean discriminative = pivotFeatureProperties.size() > intersectingFeatureProperties.size();
            jointlyDiscriminative = jointlyDiscriminative | discriminative;

            // Generates the explanation
            String explanation = "";
            if (discriminative) {
                boolean pivotHasMultipleRoles = pivotFeatureProperties.size() > 1;
                String pivotRoles = getRoleString(pivotFeatureProperties);
                explanation += MessageFormat.format("Because {0} contains {1} in propert{2} of {3} role{4} and "
                        , pivot, feature, (pivotHasMultipleRoles ? "ies" : "y")
                        , pivotRoles, (pivotHasMultipleRoles ? "s" : ""));

                if (comparisonFeatureProperties.size() > 0) {
                    boolean comparisonHasMultipleProperties = comparisonFeatureProperties.size() > 1;
                    String comparisonRoles = getRoleString(comparisonFeatureProperties);

                    explanation += MessageFormat.format("{0} contains {1} in propert{2} of {3} role{4}.",
                            comparison, feature, (comparisonHasMultipleProperties? "ies" : "y")
                            , comparisonRoles, (comparisonHasMultipleProperties ? "s" : ""));
                } else {
                    explanation += MessageFormat.format("{0} doesn''t contain {1} as one of its properties.",
                            comparison, feature);
                }
            } else if (intersectingFeatureProperties.size() > 0) {
                boolean intersectionHasMultipleProperties = intersectingFeatureProperties.size() > 1;
                String commonRoleString = getRoleString(intersectingFeatureProperties);
                explanation += MessageFormat.format("Because {0} and {1} contain {2} as propert{3} of {4} role{5}",
                        pivot, comparison, feature, (intersectionHasMultipleProperties? "ies" : "y")
                        , commonRoleString, (intersectionHasMultipleProperties? "s" : ""));
            } else if (comparisonFeatureProperties.size() > 0) {
                boolean comparisonHasMutlipleProperties = comparisonFeatureProperties.size() > 1;
                String comparisonRoles = getRoleString(comparisonFeatureProperties);
                explanation += MessageFormat.format("Because {0}''s properties don''t contain {1}," +
                                "however {2} contains {3} as propert{4} of {5} role{5}",
                        pivot, feature, comparison, feature, (comparisonHasMutlipleProperties? "ies":"y")
                        , comparisonRoles, (comparisonHasMutlipleProperties ? "s" : ""));
            } else {
                explanation += MessageFormat.format("Because {0} and {1} don''t contain {2} as a property"
                        ,pivot, comparison, feature);
            }


            if (useHTML) {
                out.println((discriminative ? "Discriminative" : "Not Discriminative"));
                out.println(explanation);
            } else {
                outputGenerator.writeString(String.valueOf(discriminative));
                outputGenerator.writeString(explanation);
                outputGenerator.writeEndArray();
            }

        }

        if (!useHTML) {
            outputGenerator.writeEndObject();
            outputGenerator.close();
            baseRequest.setHandled(true);
        }


    }

    /**
     * Accepts a List containing value,role String pairs and returns a Single string of grammatically concatonated roles.
     *
     * @param properties List of value, role String pairs.
     * @return grammatically correct concatonation of the roles in properties.
     */
    private String getRoleString(List<Property> properties) {
        String roleString = properties.get(0).getRole();
        for (int i = 1; i < properties.size() - 1; i++) {
            roleString += ", " + properties.get(i).getRole();
        }
        // Checks whether a serial comma is necessary
        if (properties.size() > 2) {
            roleString += ",";
        }
        roleString += " and " + properties.get(properties.size() - 1).getRole();
        return roleString;
    }

}

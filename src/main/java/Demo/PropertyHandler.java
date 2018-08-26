package Demo;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Handles a request to the <code>DemoServer</code> to display term properties.
 */
public class PropertyHandler extends AbstractHandler {
    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        JsonGenerator outputGenerator = new JsonFactory().createGenerator(out);
        String parameter = baseRequest.getParameter("properties");

        String pivot = baseRequest.getParameter("pivot");
        String comparison = baseRequest.getParameter("comparison");

        outputGenerator.writeStartObject();
        String[] indexNames = {"WKP_Graph", "WKT", "WN"};
        for (String indexName : indexNames) {
            outputGenerator.writeArrayFieldStart(indexName);

            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("out/indexes/" + indexName)));
            IndexSearcher searcher = new IndexSearcher(reader);

            ScoreDoc[] pivotDocs = searcher.search(new TermQuery(new Term("definiendum", pivot)), 10).scoreDocs;
            ScoreDoc[] comparisonDocs = searcher.search(new TermQuery(new Term("definiendum", comparison)), 10).scoreDocs;

            LinkedList<String> pivotProperties = new LinkedList<>();
            LinkedList<String> comparisonProperties = new LinkedList<>();

            for (ScoreDoc result : pivotDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    pivotProperties.add(field.stringValue() + "(" + field.name() + ")");
                }
            }

            if (parameter.equals("p")) {
                for (String property : pivotProperties) {
                    outputGenerator.writeString(property);
                }
                outputGenerator.writeEndArray();
                continue;
            }

            for (ScoreDoc result : comparisonDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    comparisonProperties.add(field.stringValue() + "(" + field.name() + ")");
                }
            }

            if (parameter.equals("c")) {
                for (String property : comparisonProperties) {
                    outputGenerator.writeString(property);
                }
                outputGenerator.writeEndArray();
                continue;
            }

            switch (parameter) {
                case "intersection":
                    pivotProperties.retainAll(comparisonProperties);
                    for (String property : pivotProperties) {
                        outputGenerator.writeString(property);
                    }
                    break;
                case "p-c":
                    pivotProperties.removeAll(comparisonProperties);
                    for (String property : pivotProperties) {
                        outputGenerator.writeString(property);
                    }
                    break;
                case "c-p":
                    comparisonProperties.removeAll(pivotProperties);
                    for (String property : comparisonProperties) {
                        outputGenerator.writeString(property);
                    }
                    break;
            }

            outputGenerator.writeEndArray();
        }

        outputGenerator.writeEndObject();
        outputGenerator.close();

        baseRequest.setHandled(true);
    }
}

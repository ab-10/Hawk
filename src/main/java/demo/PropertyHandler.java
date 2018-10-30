package demo;

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
    // Location of the index folder relative to the location from which the program is run
    private final String indexFolderLocation;

    public PropertyHandler(String indexFolderLocation) {
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

        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        JsonGenerator outputGenerator = new JsonFactory().createGenerator(out);

        String format = baseRequest.getAttribute("format") + "";
        Boolean useHTML = format.equalsIgnoreCase("HTML");

        String parameter, pivot, comparison;

        parameter = baseRequest.getParameter("properties");
        pivot = baseRequest.getParameter("pivot");
        comparison = baseRequest.getParameter("comparison");

        // Makes sure that all parameters were specified
        if (pivot == null | comparison == null | parameter == null) {
            if (!useHTML) {
                out.println("{Invalid request}");
            }
            baseRequest.setHandled(true);
            return;
        }

        if (useHTML) {
            response.setContentType("text/html");
        } else {
            response.setContentType("application/json");
        }

        if (!useHTML) {
            outputGenerator.writeStartObject();
        }

        String[] indexNames = {"WKP_Graph", "WKT", "WN"};
        for (String indexName : indexNames) {
            if (useHTML) {
                out.println("<h3>" + indexName + "</h3>");
            } else {
                outputGenerator.writeArrayFieldStart(indexName);
            }

            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolderLocation + indexName)));
            IndexSearcher searcher = new IndexSearcher(reader);

            ScoreDoc[] pivotDocs = searcher.search(new TermQuery(new Term("definiendum", pivot)), 50).scoreDocs;
            ScoreDoc[] comparisonDocs = searcher.search(new TermQuery(new Term("definiendum", comparison)), 50).scoreDocs;

            LinkedList<String> pivotProperties = new LinkedList<>();
            LinkedList<String> comparisonProperties = new LinkedList<>();

            for (ScoreDoc result : pivotDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    pivotProperties.add(field.stringValue() + "(" + field.name() + ")");
                }
            }

            if (useHTML) {
                out.println("<ul>");
            }

            if (parameter.equals("p")) {
                for (String property : pivotProperties) {
                    if (useHTML) {
                        out.println("<li>" + property + "</li>");
                    } else {
                        outputGenerator.writeString(property);
                    }
                }

                if (useHTML) {
                    out.println("</ul>");
                } else {
                    outputGenerator.writeEndArray();
                }
                continue;
            }

            for (ScoreDoc result : comparisonDocs) {
                for (IndexableField field : searcher.doc(result.doc).getFields()) {
                    comparisonProperties.add(field.stringValue() + "(" + field.name() + ")");
                }
            }

            if (parameter.equals("c")) {
                for (String property : comparisonProperties) {
                    if (useHTML) {
                        out.println("<li>" + property + "</li>");
                    } else {
                        outputGenerator.writeString(property);
                    }
                }
                if (useHTML) {
                    out.println("</ul>");
                } else {
                    outputGenerator.writeEndArray();
                }
                continue;
            }

            switch (parameter) {
                case "intersection":
                    pivotProperties.retainAll(comparisonProperties);
                    for (String property : pivotProperties) {
                        if (useHTML) {
                            out.println("<li>" + property + "</li>");
                        } else {
                            outputGenerator.writeString(property);
                        }
                    }
                    break;
                case "p-c":
                    pivotProperties.removeAll(comparisonProperties);
                    for (String property : pivotProperties) {
                        if (useHTML) {
                            out.println("<li>" + property + "</li>");
                        } else {
                            outputGenerator.writeString(property);
                        }
                    }
                    break;
                case "c-p":
                    comparisonProperties.removeAll(pivotProperties);
                    for (String property : comparisonProperties) {
                        if (useHTML) {
                            out.println("<li>" + property + "</li>");
                        } else {
                            outputGenerator.writeString(property);
                        }
                    }
                    break;
            }

            if (useHTML) {
                out.println("</ul>");
            } else {
                outputGenerator.writeEndArray();
            }
        }

        if (!useHTML) {
            outputGenerator.writeEndObject();
            outputGenerator.close();
            baseRequest.setHandled(true);
        }
    }
}

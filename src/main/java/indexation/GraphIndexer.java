package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import prep.Definition;
import prep.Graph;
import prep.Property;

import java.io.IOException;

/**
 * Functionality for creating a <code>Lucene Index</code> from <code>Graph</code> and writing it to disk.
 *
 * Note: before generating a new <code>Index</code>, that would override an existing one, the existing one
 * has to be manually deleted!
 *
 * @author Armins Stepanjans
 */
public class GraphIndexer {
    /**
     * Creates <code>Index</code> from <code>graph</code> and writes it to <code>destinationDir</code>.
     *
     * @param graph Graph to index
     * @param destinationDir Directory where index should be stored
     */
    public static void indexGraph(Graph graph, Directory destinationDir) throws IOException {


        Analyzer analyzer = new DefinitionAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer;
        try {
            writer = new IndexWriter(destinationDir, config);
        } catch (IOException exception) {
            System.out.println("Invalid graph location");
            return;
        }

        for (Definition currentDefinition : graph.getAllDefinitions()) {
            Document currentDocument = new Document();

            for(String currentDefiniendum : currentDefinition.getDefinienda()){
                currentDocument.add(new StringField("definiendum", currentDefiniendum, Field.Store.YES));
            }

            for (Property currentProperty : currentDefinition.getProperties()) {
                currentDocument.add(new TextField("property", currentProperty.getValue(), Field.Store.YES));
                if(currentProperty.getSubject() != "") {
                    currentDocument.add(new TextField("property", currentProperty.getSubject(), Field.Store.YES));
                }
            }
            writer.addDocument(currentDocument);

        }
        writer.close();
    }
}

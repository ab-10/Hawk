package indexation;

import edu.stanford.nlp.simple.Sentence;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import prep.Definition;
import prep.Graph;
import prep.Property;

import java.io.IOException;

/**
 * Functionality for creating a <code>Lucene Index</code> from <code>WNGraph</code> and writing it to disk.
 * <p>
 * Note: before generating a new <code>Index</code>, that would override an existing one, the existing one
 * has to be manually deleted!
 *
 * @author Armins Stepanjans
 */
public class GraphIndexer {
    /**
     * Creates <code>Index</code> from <code>WNGraph</code> and writes it to <code>destinationDir</code>.
     *
     * @param graph          WNGraph to index
     * @param destinationDir Directory where index should be stored
     */
    public static void indexGraph(Graph graph, Directory destinationDir) throws IOException, UnpopulatedGraphException {

        if (!graph.isPopulated()) {
            throw new UnpopulatedGraphException("Call populate before indexing graph!");
        }

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer;
        try {
            writer = new IndexWriter(destinationDir, config);
        } catch (IOException exception) {
            System.out.println("Invalid WNGraph location");
            return;
        }

        for (Definition currentDefinition : graph.getAllDefinitions()) {
            Document currentDocument = new Document();

            for (String currentDefiniendum : currentDefinition.getDefinienda()) {
                currentDocument.add(new TextField("property", currentDefiniendum, Field.Store.YES));
            }

            String rawGloss = "";
            String chunkedGloss = "";
            for (Property currentProperty : currentDefinition.getProperties()) {
                rawGloss += " " + currentProperty.getValue();
                chunkedGloss += " " + currentProperty.getValue().replace(" ", "_");
                currentDocument.add(new TextField(currentProperty.getRole(), currentProperty.getValue(), Field.Store.YES));

                if (currentProperty.getSubject().trim().length() > 0) {
                    rawGloss += " " + currentProperty.getSubject();
                    chunkedGloss += " " + currentProperty.getSubject().replace(" ", "_");
                }
            }
            currentDocument.add(new TextField("rawGloss", rawGloss, Field.Store.YES));
            currentDocument.add(new TextField("chunkedGloss", chunkedGloss, Field.Store.YES));
            writer.addDocument(currentDocument);

        }
        writer.close();
    }

}

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
    public static final String BLIND_FIELD_NAME = "blind"; // Name of the universal, blind field

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
            Document blindDoc = new Document(); // Document used for blind vote
            Document roleDoc = new Document();  // Document used for role based vote


            for (String currentDefiniendum : currentDefinition.getDefinienda()) {
                roleDoc.add(new TextField("definiendum", currentDefiniendum, Field.Store.YES));

                for (String lemmaDefiniendum : new Sentence(currentDefiniendum).lemmas()) {
                    blindDoc.add(new TextField(BLIND_FIELD_NAME, lemmaDefiniendum, Field.Store.YES));
                }
            }

            for (Property currentProperty : currentDefinition.getProperties()) {
                roleDoc.add(new TextField(currentProperty.getRole(), currentProperty.getValue(), Field.Store.YES));

                if (currentProperty.getValue().trim().length() > 0) {
                    for (String lemmaValue : new Sentence(currentProperty.getValue()).lemmas()) {
                        blindDoc.add(new TextField(BLIND_FIELD_NAME, lemmaValue, Field.Store.YES));
                    }
                }
                if (currentProperty.getSubject().trim().length() > 0) {
                    for (String lemmaProperty : new Sentence(currentProperty.getValue()).lemmas()) {
                        blindDoc.add(new TextField(BLIND_FIELD_NAME, lemmaProperty, Field.Store.YES));
                    }
                }
            }
            writer.addDocument(roleDoc);

        }
        writer.close();
    }

}

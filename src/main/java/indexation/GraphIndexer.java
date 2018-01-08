package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import prep.Definition;
import prep.Graph;
import prep.Property;

import java.io.FileWriter;
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
     * Creates <code>Index</code> from <code>graph</code> and writes it to <code>directory</code>.
     * @param graph
     * @param directory
     */
    public static void indexGraph(Graph graph, Directory directory) throws IOException{


        Analyzer analyzer = new DefinitionAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer;
        try {
            writer = new IndexWriter(directory, config);
        }catch (IOException exception){
            System.out.println("Invalid graph location");
            return;
        }

        for(Definition currentDefinition : graph.getAllDefinitions()){
            Document currentDocument = new Document();
            // Ensures that definitions consisting of multiple synonymous definienda
            // have all definienda recorded in separate fields

            for(String definiendum : currentDefinition.getDefiniendum().split("_+")) {
                currentDocument.add(new StringField("definiendum", definiendum.toLowerCase(), Field.Store.YES));
            }

            for(Property currentProperty : currentDefinition.getProperties()){
                // StringField allows for comparisons in between substrings of the field,
                // while TextField treats the entire field as single value
                for(String term : currentProperty.toString().split(" ")) {
                    currentDocument.add(new StringField("property", term.toLowerCase(), Field.Store.YES));
                }

            }
            writer.addDocument(currentDocument);

        }
        writer.close();

    }
}

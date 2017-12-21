package prep;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;

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


        Analyzer analyzer = new StandardAnalyzer();
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
            currentDocument.add(new StringField("definiendum", currentDefinition.getDefiniendum(), Field.Store.YES));
            for(Property currentProperty : currentDefinition.getProperties()){
                // StringField allows for comparisons in between substrings of the field,
                // while TextField treats the entire field as single value
                currentDocument.add(new StringField(currentProperty.getRole(), currentProperty.getValue(), Field.Store.YES));
            }
            writer.addDocument(currentDocument);

        }
        writer.close();


    }
}

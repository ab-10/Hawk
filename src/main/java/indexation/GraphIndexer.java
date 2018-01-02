package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
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

import java.io.FileWriter;
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

        FileWriter fileWriter = new FileWriter("src/main/resources/tokenizerTest.txt");

        int definitionsParsed = 0;
        for(Definition currentDefinition : graph.getAllDefinitions()){
            Document currentDocument = new Document();
            // Ensures that definitions consisting of multiple synonymous definienda
            // have all definienda recorded in separate fields
            String[] definienda = currentDefinition.getDefiniendum().split("__");
            for(String currentDefiniendum : definienda){

                if(definitionsParsed < 10) {
                    fileWriter.write("For definition of " + currentDefiniendum + "\n");
                    fileWriter.write("Following definiendum tokens were made:\n");
                    TokenStream currentTk = analyzer.tokenStream("definiendum", currentDefiniendum);
                    OffsetAttribute currentOa = currentTk.addAttribute(OffsetAttribute.class);
                    parseTS(fileWriter, currentTk, currentOa);
                }

                currentDocument.add(new StringField("definiendum",
                        currentDefiniendum.replace("_", " ").toLowerCase(), Field.Store.YES));
            }

            for(Property currentProperty : currentDefinition.getProperties()){
                // StringField allows for comparisons in between substrings of the field,
                // while TextField treats the entire field as single value
                currentDocument.add(new StringField("property", currentProperty.getValue().toLowerCase(), Field.Store.YES));

                if(definitionsParsed < 10) {
                    fileWriter.write("\n");
                    fileWriter.write("and following property tokens were made\n");
                    TokenStream currentTk = analyzer.tokenStream("property", currentProperty.getValue());
                    OffsetAttribute currentOa = currentTk.addAttribute(OffsetAttribute.class);
                    parseTS(fileWriter, currentTk, currentOa);
                    fileWriter.write("\n");
                }
            }
            definitionsParsed++;
            writer.addDocument(currentDocument);

        }
        writer.close();


    }

    private static void parseTS(FileWriter fileWriter, TokenStream currentTk, OffsetAttribute currentOa) throws IOException {
        try{
            currentTk.reset();
            while (currentTk.incrementToken()){
                fileWriter.write("token: " + currentTk.reflectAsString(true) + "\n");
                fileWriter.write("token start offset: " + currentOa.startOffset() + "\n");
                fileWriter.write("token start offset: " + currentOa.endOffset() + "\n");
            }
            currentTk.end();
        }finally {
            currentTk.close();
        }
    }
}

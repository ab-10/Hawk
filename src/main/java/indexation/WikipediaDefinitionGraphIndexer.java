package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Class for indexing Wikipedia Definition Graphs,
 * for indexing Wikipedia definitions extracted using word class lattices,
 * please, use the methods provided in <code>WikipediaIndexer</code>.
 */
public class WikipediaDefinitionGraphIndexer {
    public static void main(String args[]) throws IOException {
        File wikipediaCSV = new File("src/main/resources/WKP_DSR_model_CSV.csv");
        File wikipediaFixed = new File("src/main/resources/WKP_filtered_classified_fixed.txt");
        Directory indexDir = FSDirectory.open(Paths.get("out/indexes/WKP_Graph"));
        indexDefinitions(wikipediaCSV, wikipediaFixed, indexDir);
    }

    /**
     * Method for indexing definition roles from wikipediaFixed using glosses from WikipediaCSV.
     * <p>
     * The two definition sources are necessary, because wikipediaFixed contains improved (fixed)
     * glosses, with no definienda,
     * while wikipediaCSV contains the definienda in the same order as wikipediaFixed.
     *
     * @param wikipediaCSV   location of WKP_DSR_model_CSV.csv
     * @param wikipediaFixed location of WKP_filtered_classified_fixed.txt
     * @param destination    where the index should be written
     * @throws IOException
     */
    public static void indexDefinitions(File wikipediaCSV, File wikipediaFixed, Directory destination) throws IOException {
        Scanner wkpCSV = new Scanner(wikipediaCSV);
        Scanner wkpFixed = new Scanner(wikipediaFixed);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(destination, config);

        while (wkpCSV.hasNext()) {
            Document currentDocument = new Document();

            String currentDefiniendum = wkpCSV.nextLine().split(";")[2];
            currentDocument.add(new TextField("definiendum", currentDefiniendum, Field.Store.YES));

            String currentProperty = wkpFixed.nextLine();
            // Seeks the beginning of the definition
            while (!currentProperty.equals("BOS O")) {
                currentProperty = wkpFixed.nextLine();
            }
            // Locates the first property
            currentProperty = wkpFixed.nextLine();

            while (!currentProperty.equals("EOS O")) {
                String currentValue = currentProperty.split(" ")[0];
                String currentRole = currentProperty.split(" ")[1];

                currentDocument.add(new TextField(currentRole, currentValue, Field.Store.YES));

                // The last definition doesn't contain EOS O
                if (wkpFixed.hasNext()) {
                    currentProperty = wkpFixed.nextLine();
                } else {
                    currentProperty = "EOS O";
                }
            }

            writer.addDocument(currentDocument);
        }
        writer.close();
    }
}

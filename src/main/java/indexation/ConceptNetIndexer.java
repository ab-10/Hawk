package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class ConceptNetIndexer {
    public static void main(String[] args) throws IOException {
        indexConcepts(new File("src/main/resources/cleanConcepts.csv"), FSDirectory.open(Paths.get("src/main/resources/CleanConceptNet")));
    }

    // TODO: clean conceptNet in the script
    public static void indexConcepts(File cleanedConceptNet, Directory indexDirectory) throws IOException {
        Scanner conceptScanner = new Scanner(cleanedConceptNet);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDirectory, config);

        while (conceptScanner.hasNext()) {
            String[] currentLine = conceptScanner.nextLine().split(",");
            Document currentDocument = new Document();
            currentDocument.add(new StringField("property", currentLine[1], Field.Store.YES));
            currentDocument.add(new StringField("property", currentLine[2], Field.Store.YES));
            currentDocument.add(new StringField("role", currentLine[0], Field.Store.YES));

            writer.addDocument(currentDocument);
        }
        writer.close();

    }
}

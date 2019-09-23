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
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class WNDefinitionIndexer {
    public static void main(String[] args){
        File cleanedWNData = new File("src/main/resources/cleanedWNData.combined");
        try {
            Directory indexDir = FSDirectory.open(Paths.get("src/main/resources/WNDefinitions"));
            indexDefinitions(cleanedWNData, indexDir);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void indexDefinitions(File cleanedWNData, Directory destination) throws IOException {
        Scanner wnScanner = new Scanner(cleanedWNData);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(destination, config);

        while(wnScanner.hasNext()){
            String[] currentLine = wnScanner.nextLine().split("\\|");

            Document currentDoc = new Document();

            for(String definiendum : currentLine[0].split(",")){
                currentDoc.add(new TextField("definiendum", definiendum.replace("_", " "),
                        Field.Store.YES));
            }

            currentDoc.add(new TextField("definition", currentLine[1], Field.Store.YES));

            for(String lemma : new Sentence(currentLine[1]).lemmas()){
                currentDoc.add(new TextField("property", lemma, Field.Store.YES));
            }

            writer.addDocument(currentDoc);
        }

        writer.close();
    }
}

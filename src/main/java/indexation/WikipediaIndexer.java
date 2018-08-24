package indexation;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WikipediaIndexer {
    public static void indexWikipediaDefinitions(File source, Directory destination) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(source));

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(destination, config);

        String currentLine = reader.readLine();
        Document currentDocument = new Document();
        String[] properties;
        while(currentLine != null){
            if(currentLine.charAt(0) != '#'){
                properties = currentLine.split("\t");

                String target = properties[0].substring(0, properties[0].indexOf(':'));
                currentDocument.add(new StringField("property", target, Field.Store.YES));

                // skip the entry number 1, because it's always TARGET
                for(int i = 2; i < properties.length; i++){
                    int propertyInd = StringUtils.ordinalIndexOf(properties[i], "_", 2);
                    if(propertyInd != -1) {
                        String currentProperty = properties[i].substring(propertyInd + 1);
                        currentDocument.add(new StringField("property", currentProperty.toLowerCase()
                                , Field.Store.YES));
                    }
                }
                writer.addDocument(currentDocument);
                currentDocument = new Document();
            }
            currentLine = reader.readLine();
        }
        reader.close();
        writer.close();
    }
}

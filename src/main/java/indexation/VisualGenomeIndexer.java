package indexation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Indexes attributes.json file, from Visual Genome
 */
public class VisualGenomeIndexer {
    public static void indexGenomeAttributes(File source, Directory destination){
        Scanner jsonFileScanner;
        try {
            jsonFileScanner = new Scanner(source);
        } catch (FileNotFoundException e){
            System.err.println("Invalid JSON file location");
            return;
        }

        IndexWriter writer;
        Analyzer analyzer = new DefinitionAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try {
            writer = new IndexWriter(destination, config);
        } catch (IOException e){
            System.err.println("Invalid destination");
            return;
        }

        // \Z matches the end of string
        String data = jsonFileScanner.useDelimiter("\\Z").next();
        ObjectMapper mapper = new ObjectMapper();
        VGImage[] images;
        try {
            images = mapper.readValue(data, VGImage[].class);
        } catch(IOException e){
            System.err.println("Failed to parse data using Jackson");
            System.err.println(e);
            return;
        }

        for(VGImage currentImage : images){
            for(Attribute currentAttribute : currentImage.attributes){
                if((currentAttribute.names.length > 0) && (currentAttribute.attributes.length > 0)) {
                    Document currentDocument = new Document();

                    for (String currentName : currentAttribute.names) {
                        currentDocument.add(new StringField("name", currentName, Field.Store.YES));
                    }

                    for (String subattribute : currentAttribute.attributes) {
                        currentDocument.add(new StringField("attribute", subattribute, Field.Store.YES));
                    }

                    try {
                        writer.addDocument(currentDocument);
                    } catch (IOException e) {
                        System.err.println("Failed to index document");
                        System.err.println(e);
                    }
                }
            }
        }

        try {
            writer.close();
        } catch (IOException e){
            System.err.println("Failed to close writer");
            System.err.println(e);
        }

    }

    public static void main(String[] args){
    }
}

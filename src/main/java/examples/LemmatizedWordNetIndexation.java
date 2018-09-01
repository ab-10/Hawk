package examples;

import indexation.GraphIndexer;
import indexation.UnpopulatedGraphException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import prep.Graph;
import prep.WNGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import static analysis.DictionaryClassifiers.wordNetVote;
import static indexation.GraphIndexer.indexGraph;

public class LemmatizedWordNetIndexation {
    public static void main(String args[]) throws IOException, UnpopulatedGraphException {
        // creates a Lucene index from lemmatized WN graph
        String indexLocation = "out/indexes/WN";
        Directory indexDir = FSDirectory.open(Paths.get(indexLocation));
        Graph wnGraph = new WNGraph("WN_DSR_model_XML.rdf");
        wnGraph.populate();
        indexGraph(wnGraph, indexDir);

        // evaluates the index against sample data
        FileWriter resultWriter = new FileWriter("src/main/resources/WN.results");
        Scanner taskScanner = new Scanner(new File("src/main/resources/truth.txt"));
        while(taskScanner.hasNext()){
            String[] currentLine = taskScanner.nextLine().split(",");
            int result = wordNetVote(currentLine[0], currentLine[1], currentLine[2], indexLocation);
            resultWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2] + "," + result + "\n");
        }
        taskScanner.close();
        resultWriter.close();
    }
}

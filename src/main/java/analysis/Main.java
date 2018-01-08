package analysis;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import prep.Graph;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

import static indexation.GraphIndexer.indexGraph;

public class Main {
    private static Directory graphDirectory;
    public static void main(String[] args) throws Exception{
        graphDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "index"));
        Graph graph = new Graph("WN_DSR_model_XML.rdf");
        indexGraph(graph, graphDirectory);
        Scanner fileScanner = new Scanner(new File("src/main/resources/validation.txt"));
        FileWriter resultWriter = new FileWriter("src/main/resources/standardAnalyzerResults.txt");
        while(fileScanner.hasNext()){
            String[] currentLine = fileScanner.nextLine().split(",");
            resultWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                    + "," + compare(currentLine[0], currentLine[1], currentLine[2])
                    + System.getProperty("line.separator"));

        }
        resultWriter.close();

    }

    public static int compare(String pivot, String comparison, String feature) throws Exception{
        BooleanQuery.Builder builderPivot = new BooleanQuery.Builder();
        BooleanQuery.Builder builderComparison = new BooleanQuery.Builder();
        builderPivot.add(new TermQuery(new Term("definiendum", pivot)), BooleanClause.Occur.MUST);
        builderPivot.add(new TermQuery(new Term("property", feature)), BooleanClause.Occur.MUST);

        builderComparison.add(new TermQuery(new Term("definiendum", comparison)), BooleanClause.Occur.MUST);
        builderComparison.add(new TermQuery(new Term("property", feature)), BooleanClause.Occur.MUST);

        BooleanQuery queryPivot = builderPivot.build();
        BooleanQuery queryComparison = builderComparison.build();

        DirectoryReader reader = DirectoryReader.open(graphDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        ScoreDoc[] resultsComparison = searcher.search(queryComparison, 10).scoreDocs;

        if(resultsPivot.length != 0 && resultsComparison.length == 0){
            return 1;
        }else{
            return 0;
        }

        /*
        System.out.println("Results for " + word1);
        for(ScoreDoc result : resultsPivot){
            Document currentDocument = searcher.doc(result.doc);
            System.out.println("Definition: " + currentDocument.get("definiendum"));
            System.out.println(currentDocument);
            System.out.println(result);
        }

        System.out.println("Results for " + word2);
        for(ScoreDoc result : resultsComparison){
            Document currentDocument = searcher.doc(result.doc);
            System.out.println("Definition: " + currentDocument.getField("definiendum"));
            System.out.println(currentDocument);
            System.out.println(result);
        }
        */

    }
}

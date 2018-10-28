package examples;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class VocabularyTester {
    public static void main(String args[]) throws IOException {
        String[] indexes = new String[]{"WN", "WKT", "WKP_Graph"};
        for (String index : indexes) {
            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("out/indexes/" + index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Scanner taskScanner = new Scanner(new File("src/main/resources/truth.txt"));
            int triplesIn = 0;
            int triplesOut = 0;
            int discTriplesIn = 0;
            int discTriplesOut = 0;
            int discPivotsIn = 0;
            int discPivotsOut = 0;

            while (taskScanner.hasNext()) {
                String[] currentLine = taskScanner.nextLine().split(",");
                String pivot = currentLine[0];
                String comparison = currentLine[1];

                TermQuery pivotQuery = new TermQuery(new Term("definiendum", pivot));
                TermQuery comparisonQuery = new TermQuery(new Term("definiendum", comparison));

                int resultsPivot = (int) searcher.search(pivotQuery, 1).totalHits;
                int resultsComparison = (int) searcher.search(comparisonQuery, 1).totalHits;

                if(currentLine[3].equals("1")){
                    if(resultsPivot > 0){
                        discPivotsIn += 1;
                    }else {
                        discPivotsOut += 1;
                    }

                    if(resultsPivot > 0 & resultsComparison > 0){
                        discTriplesIn += 1;
                    }else {
                        discTriplesOut += 1;
                    }
                }

                if(resultsPivot > 1 & resultsComparison > 1){
                    triplesIn += 1;
                }else {
                    triplesOut += 1;
                }

            }
            System.out.println(index);
            System.out.println("Triples in " + triplesIn);
            System.out.println("Triples out " + triplesOut);
            System.out.println("Disc triples in " + discTriplesIn);
            System.out.println("Disc triples out " + discTriplesOut);
            System.out.println("Disc piv in " + discPivotsIn);
            System.out.println("Disc piv out " + discPivotsOut);
        }
    }
}
